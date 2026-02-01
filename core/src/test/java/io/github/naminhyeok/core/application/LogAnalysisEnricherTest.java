package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.support.fake.FakeIpInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class LogAnalysisEnricherTest {

    private FakeIpInfoClient fakeIpInfoClient;
    private LogAnalysisEnricher logAnalysisEnricher;

    @BeforeEach
    void setUp() {
        fakeIpInfoClient = new FakeIpInfoClient();
        IpInfoReader ipInfoReader = new IpInfoReader(fakeIpInfoClient);
        logAnalysisEnricher = new LogAnalysisEnricher(ipInfoReader);
    }

    @Test
    void LogAnalysis에_IP_정보를_병합하여_LogAnalysisResult를_생성한다() {
        // given
        fakeIpInfoClient
            .withIpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
            .withIpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare");

        List<AccessLog> accessLogs = List.of(
            createAccessLog("8.8.8.8"),
            createAccessLog("8.8.8.8"),
            createAccessLog("1.1.1.1")
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        LogAnalysisResult result = logAnalysisEnricher.enrich(logAnalysis, 10);

        // then
        then(result.logAnalysis()).isEqualTo(logAnalysis);
        then(result.getIpInfo("8.8.8.8").country()).isEqualTo("US");
        then(result.getIpInfo("1.1.1.1").country()).isEqualTo("AU");
    }

    @Test
    void 존재하지_않는_IP는_unknown_정보를_반환한다() {
        // given
        List<AccessLog> accessLogs = List.of(createAccessLog("192.168.0.1"));
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        LogAnalysisResult result = logAnalysisEnricher.enrich(logAnalysis, 10);

        // then
        IpInfo ipInfo = result.getIpInfo("192.168.0.1");
        then(ipInfo.isUnknown()).isTrue();
        then(ipInfo.ip()).isEqualTo("192.168.0.1");
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
