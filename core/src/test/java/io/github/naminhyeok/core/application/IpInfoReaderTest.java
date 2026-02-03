package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.support.fake.FakeIpInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

class IpInfoReaderTest {

    private FakeIpInfoClient fakeIpInfoClient;
    private Cache<String, IpInfo> cache;
    private IpInfoReader ipInfoReader;

    @BeforeEach
    void setUp() {
        fakeIpInfoClient = new FakeIpInfoClient();
        cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
        ipInfoReader = new IpInfoReader(cache, fakeIpInfoClient);
    }

    @Test
    void 단건_IP_정보를_조회할_수_있다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withIpInfo(ip, "US", "California", "Mountain View", "Google LLC");

        // when
        IpInfo foundIpInfo = ipInfoReader.read(ip);

        // then
        then(foundIpInfo)
            .extracting(IpInfo::ip, IpInfo::country, IpInfo::city)
            .containsExactly(ip, "US", "Mountain View");
    }

    @Test
    void 여러_IP_정보를_배치로_조회할_수_있다() {
        // given
        fakeIpInfoClient
            .withIpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
            .withIpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare");

        List<String> ips = List.of("8.8.8.8", "1.1.1.1");

        // when
        Map<String, IpInfo> ipInfoMap = ipInfoReader.readAll(ips);

        // then
        then(ipInfoMap).hasSize(2);
        then(ipInfoMap.get("8.8.8.8").country()).isEqualTo("US");
        then(ipInfoMap.get("1.1.1.1").country()).isEqualTo("AU");
    }

    @Test
    void 중복된_IP는_한_번만_조회한다() {
        // given
        fakeIpInfoClient
            .withIpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
            .withIpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare");

        List<String> ipsWithDuplicates = List.of("8.8.8.8", "8.8.8.8", "1.1.1.1");

        // when
        Map<String, IpInfo> ipInfoMap = ipInfoReader.readAll(ipsWithDuplicates);

        // then
        then(ipInfoMap).hasSize(2);
        then(fakeIpInfoClient.getCallCount("8.8.8.8")).isEqualTo(1);
        then(fakeIpInfoClient.getCallCount("1.1.1.1")).isEqualTo(1);
        then(fakeIpInfoClient.getTotalCallCount()).isEqualTo(2);
    }

    @Test
    void 등록되지_않은_IP는_unknown_정보를_반환한다() {
        // given
        String unknownIp = "192.168.0.1";

        // when
        IpInfo ipInfo = ipInfoReader.read(unknownIp);

        // then
        then(ipInfo.isUnknown()).isTrue();
        then(ipInfo.ip()).isEqualTo(unknownIp);
    }

    @Test
    void 캐시된_IP는_클라이언트를_호출하지_않는다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withIpInfo(ip, "US", "California", "Mountain View", "Google LLC");

        // when - 첫 번째 조회 (MISS)
        ipInfoReader.read(ip);
        int callCountAfterFirstRead = fakeIpInfoClient.getCallCount(ip);

        // when - 두 번째 조회 (HIT)
        IpInfo cachedResult = ipInfoReader.read(ip);
        int callCountAfterSecondRead = fakeIpInfoClient.getCallCount(ip);

        // then
        then(callCountAfterFirstRead).isEqualTo(1);
        then(callCountAfterSecondRead).isEqualTo(1);
        then(cachedResult.country()).isEqualTo("US");
    }

    @Test
    void unknown_결과는_캐싱하지_않는다() {
        // given
        String unknownIp = "192.168.0.1";

        // when - 첫 번째 조회
        ipInfoReader.read(unknownIp);
        int callCountAfterFirstRead = fakeIpInfoClient.getCallCount(unknownIp);

        // when - 두 번째 조회 (캐싱되지 않아서 다시 호출)
        ipInfoReader.read(unknownIp);
        int callCountAfterSecondRead = fakeIpInfoClient.getCallCount(unknownIp);

        // then
        then(callCountAfterFirstRead).isEqualTo(1);
        then(callCountAfterSecondRead).isEqualTo(2);
    }

    @Test
    void 여러_요청에서_캐시가_공유된다() {
        // given
        fakeIpInfoClient
            .withIpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
            .withIpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare");

        // when - 첫 번째 요청
        ipInfoReader.readAll(List.of("8.8.8.8", "1.1.1.1"));

        // when - 두 번째 요청 (캐시에서 조회)
        ipInfoReader.readAll(List.of("8.8.8.8", "1.1.1.1"));

        // then - 각 IP는 한 번씩만 호출
        then(fakeIpInfoClient.getCallCount("8.8.8.8")).isEqualTo(1);
        then(fakeIpInfoClient.getCallCount("1.1.1.1")).isEqualTo(1);
        then(fakeIpInfoClient.getTotalCallCount()).isEqualTo(2);
    }

    @Test
    void IpInfoClient가_null을_반환하면_unknown을_반환한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withNullResponse(ip);

        // when
        IpInfo result = ipInfoReader.read(ip);

        // then
        then(result.isUnknown()).isTrue();
        then(result.ip()).isEqualTo(ip);
    }
}
