package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.domain.LogStreamAggregator;
import io.github.naminhyeok.core.support.fake.FakePendingIpQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.BDDAssertions.then;

class LogAnalysisEnricherTest {

    private Cache<String, IpInfo> cache;
    private LogAnalysisEnricher logAnalysisEnricher;

    @BeforeEach
    void setUp() {
        FakePendingIpQueue fakePendingIpQueue = new FakePendingIpQueue();
        cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
        IpInfoReader ipInfoReader = new IpInfoReader(cache, fakePendingIpQueue);
        logAnalysisEnricher = new LogAnalysisEnricher(ipInfoReader);
    }

    @Test
    void LogAnalysisAggregate에_IP_정보를_병합하여_LogAnalysisResult를_생성한다() {
        // given - 캐시에 IP 정보 미리 적재 (비동기 워커가 처리한 것처럼)
        cache.put("8.8.8.8", new IpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC"));
        cache.put("1.1.1.1", new IpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare"));

        LogAnalysisAggregate aggregate = createAggregateWithIps("8.8.8.8", "8.8.8.8", "1.1.1.1");

        // when
        LogAnalysisResult result = logAnalysisEnricher.enrich(aggregate, 10);

        // then
        then(result.aggregate()).isEqualTo(aggregate);
        then(result.getIpInfo("8.8.8.8").country()).isEqualTo("US");
        then(result.getIpInfo("1.1.1.1").country()).isEqualTo("AU");
    }

    @Test
    void 캐시에_없는_IP는_unknown_정보를_반환한다() {
        // given - 캐시에 IP 정보 없음
        LogAnalysisAggregate aggregate = createAggregateWithIps("192.168.0.1");

        // when
        LogAnalysisResult result = logAnalysisEnricher.enrich(aggregate, 10);

        // then
        IpInfo ipInfo = result.getIpInfo("192.168.0.1");
        then(ipInfo.isUnknown()).isTrue();
        then(ipInfo.ip()).isEqualTo("192.168.0.1");
    }

    private LogAnalysisAggregate createAggregateWithIps(String... clientIps) {
        LogStreamAggregator aggregator = new LogStreamAggregator();
        for (String clientIp : clientIps) {
            aggregator.accumulate(createAccessLog(clientIp));
        }
        return aggregator.finish();
    }

    private AccessLog createAccessLog(String clientIp) {
        return new AccessLog(
            LocalDateTime.now(),
            clientIp,
            HttpMethod.GET,
            "/api/test",
            "Mozilla/5.0",
            HttpStatusCode.valueOf(200),
            "HTTP/1.1",
            100L,
            200L,
            50L,
            "TLSv1.2",
            "/api/test"
        );
    }
}
