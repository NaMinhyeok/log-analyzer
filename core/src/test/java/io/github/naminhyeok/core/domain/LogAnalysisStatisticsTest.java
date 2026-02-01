package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class LogAnalysisStatisticsTest {

    @Test
    void LogAnalysis로부터_통계를_계산할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/products", "192.168.1.2", 404),
            createAccessLog("/api/orders", "192.168.1.3", 500)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        LogAnalysisStatistics statistics = logAnalysis.calculateStatistics(10);

        // then
        then(statistics.totalRequests()).isEqualTo(4);
        then(statistics.statusCodeDistribution())
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(50.0, 25.0, 25.0);
        then(statistics.topPaths()).hasSize(3);
        then(statistics.topStatusCodes()).hasSize(3);
        then(statistics.topClientIps()).hasSize(3);
    }

    @Test
    void 빈_로그_리스트에_대한_통계를_계산할_수_있다() {
        // given
        LogAnalysis logAnalysis = new LogAnalysis(List.of(), List.of());

        // when
        LogAnalysisStatistics statistics = logAnalysis.calculateStatistics(10);

        // then
        then(statistics.totalRequests()).isEqualTo(0);
        then(statistics.statusCodeDistribution())
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(0.0, 0.0, 0.0, 0.0);
        then(statistics.topPaths()).isEmpty();
        then(statistics.topStatusCodes()).isEmpty();
        then(statistics.topClientIps()).isEmpty();
    }

    @Test
    void topN_파라미터로_상위_항목_개수를_제한할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/products", "192.168.1.2", 201),
            createAccessLog("/api/orders", "192.168.1.3", 404),
            createAccessLog("/api/payments", "192.168.1.4", 500),
            createAccessLog("/api/reviews", "192.168.1.5", 503)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        LogAnalysisStatistics statistics = logAnalysis.calculateStatistics(2);

        // then
        then(statistics.topPaths()).hasSize(2);
        then(statistics.topStatusCodes()).hasSize(2);
        then(statistics.topClientIps()).hasSize(2);
    }

    private AccessLog createAccessLog(String requestUri, String clientIp, int statusCode) {
        return new AccessLog(
            LocalDateTime.of(2026, 1, 29, 10, 30, 0),
            clientIp,
            HttpMethod.GET,
            requestUri,
            "Mozilla/5.0",
            HttpStatusCode.valueOf(statusCode),
            "HTTP/1.1",
            100L,
            500L,
            50L,
            "TLSv1.2",
            requestUri
        );
    }
}
