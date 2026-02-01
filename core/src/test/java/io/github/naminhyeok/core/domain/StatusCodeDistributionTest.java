package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class StatusCodeDistributionTest {

    @Test
    void HTTP_상태코드_분포를_계산할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog(200),  // 2xx
            createAccessLog(201),  // 2xx
            createAccessLog(301),  // 3xx
            createAccessLog(404),  // 4xx
            createAccessLog(500)   // 5xx
        );

        // when
        StatusCodeDistribution distribution = StatusCodeDistribution.from(accessLogs);

        // then
        then(distribution)
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(40.0, 20.0, 20.0, 20.0);
    }

    @Test
    void 요청이_없으면_모든_비율이_0이다() {
        // given
        List<AccessLog> accessLogs = List.of();

        // when
        StatusCodeDistribution distribution = StatusCodeDistribution.from(accessLogs);

        // then
        then(distribution)
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(0.0, 0.0, 0.0, 0.0);
    }

    @Test
    void 특정_상태코드만_있으면_해당_비율이_100퍼센트다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog(200),
            createAccessLog(201),
            createAccessLog(204)
        );

        // when
        StatusCodeDistribution distribution = StatusCodeDistribution.from(accessLogs);

        // then
        then(distribution)
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(100.0, 0.0, 0.0, 0.0);
    }

    private AccessLog createAccessLog(int statusCode) {
        return new AccessLog(
            LocalDateTime.of(2026, 1, 29, 10, 30, 0),
            "192.168.1.1",
            HttpMethod.GET,
            "/api/users",
            "Mozilla/5.0",
            HttpStatusCode.valueOf(statusCode),
            "HTTP/1.1",
            100L,
            500L,
            50L,
            "TLSv1.2",
            "/api/users"
        );
    }
}
