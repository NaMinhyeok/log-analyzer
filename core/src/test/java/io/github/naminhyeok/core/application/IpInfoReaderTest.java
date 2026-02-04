package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.support.fake.FakePendingIpQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

class IpInfoReaderTest {

    private FakePendingIpQueue fakePendingIpQueue;
    private Cache<String, IpInfo> cache;
    private IpInfoReader ipInfoReader;

    @BeforeEach
    void setUp() {
        fakePendingIpQueue = new FakePendingIpQueue();
        cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
        ipInfoReader = new IpInfoReader(cache, fakePendingIpQueue);
    }

    @Test
    void 캐시에_있는_IP는_즉시_반환한다() {
        // given
        String ip = "8.8.8.8";
        IpInfo cachedInfo = new IpInfo(ip, "US", "California", "Mountain View", "Google LLC");
        cache.put(ip, cachedInfo);

        // when
        IpInfo foundIpInfo = ipInfoReader.read(ip);

        // then
        then(foundIpInfo)
            .extracting(IpInfo::ip, IpInfo::country, IpInfo::city)
            .containsExactly(ip, "US", "Mountain View");
        then(fakePendingIpQueue.wasOffered(ip)).isFalse();
    }

    @Test
    void 캐시_미스_시_큐에_적재하고_unknown을_반환한다() {
        // given
        String ip = "8.8.8.8";

        // when
        IpInfo foundIpInfo = ipInfoReader.read(ip);

        // then
        then(foundIpInfo.isUnknown()).isTrue();
        then(foundIpInfo.ip()).isEqualTo(ip);
        then(fakePendingIpQueue.wasOffered(ip)).isTrue();
    }

    @Test
    void 여러_IP_조회_시_캐시_히트는_즉시_반환하고_미스는_큐에_적재한다() {
        // given
        String cachedIp = "8.8.8.8";
        String uncachedIp = "1.1.1.1";
        cache.put(cachedIp, new IpInfo(cachedIp, "US", "California", "Mountain View", "Google LLC"));

        List<String> ips = List.of(cachedIp, uncachedIp);

        // when
        Map<String, IpInfo> ipInfoMap = ipInfoReader.readAll(ips);

        // then
        then(ipInfoMap).hasSize(2);
        then(ipInfoMap.get(cachedIp).country()).isEqualTo("US");
        then(ipInfoMap.get(uncachedIp).isUnknown()).isTrue();
        then(fakePendingIpQueue.wasOffered(cachedIp)).isFalse();
        then(fakePendingIpQueue.wasOffered(uncachedIp)).isTrue();
    }

    @Test
    void 중복된_IP는_한_번만_큐에_적재한다() {
        // given
        List<String> ipsWithDuplicates = List.of("8.8.8.8", "8.8.8.8", "1.1.1.1");

        // when
        Map<String, IpInfo> ipInfoMap = ipInfoReader.readAll(ipsWithDuplicates);

        // then
        then(ipInfoMap).hasSize(2);
        then(fakePendingIpQueue.getOfferCount("8.8.8.8")).isEqualTo(1);
        then(fakePendingIpQueue.getOfferCount("1.1.1.1")).isEqualTo(1);
    }

    @Test
    void 캐시된_IP는_큐에_적재하지_않는다() {
        // given
        String ip = "8.8.8.8";
        cache.put(ip, new IpInfo(ip, "US", "California", "Mountain View", "Google LLC"));

        // when - 첫 번째 조회 (HIT)
        ipInfoReader.read(ip);

        // when - 두 번째 조회 (HIT)
        IpInfo cachedResult = ipInfoReader.read(ip);

        // then
        then(cachedResult.country()).isEqualTo("US");
        then(fakePendingIpQueue.getTotalOfferCount()).isEqualTo(0);
    }

    @Test
    void 캐시_미스는_매번_큐에_적재_시도한다() {
        // given
        String unknownIp = "192.168.0.1";

        // when - 첫 번째 조회
        ipInfoReader.read(unknownIp);
        int offerCountAfterFirstRead = fakePendingIpQueue.getOfferCount(unknownIp);

        // when - 두 번째 조회 (FakePendingIpQueue는 중복 허용 안함)
        ipInfoReader.read(unknownIp);
        int offerCountAfterSecondRead = fakePendingIpQueue.getOfferCount(unknownIp);

        // then
        then(offerCountAfterFirstRead).isEqualTo(1);
        then(offerCountAfterSecondRead).isEqualTo(2); // 시도 횟수
    }

    @Test
    void 여러_요청에서_캐시가_공유된다() {
        // given
        cache.put("8.8.8.8", new IpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC"));
        cache.put("1.1.1.1", new IpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare"));

        // when - 첫 번째 요청
        ipInfoReader.readAll(List.of("8.8.8.8", "1.1.1.1"));

        // when - 두 번째 요청 (캐시에서 조회)
        ipInfoReader.readAll(List.of("8.8.8.8", "1.1.1.1"));

        // then - 큐에 적재되지 않음 (모두 캐시 히트)
        then(fakePendingIpQueue.getTotalOfferCount()).isEqualTo(0);
    }

    @Test
    void 캐시에_unknown이_저장되어_있으면_그대로_반환한다() {
        // given
        String ip = "8.8.8.8";
        cache.put(ip, IpInfo.unknown(ip));

        // when
        IpInfo result = ipInfoReader.read(ip);

        // then
        then(result.isUnknown()).isTrue();
        then(result.ip()).isEqualTo(ip);
        then(fakePendingIpQueue.wasOffered(ip)).isFalse();
    }
}
