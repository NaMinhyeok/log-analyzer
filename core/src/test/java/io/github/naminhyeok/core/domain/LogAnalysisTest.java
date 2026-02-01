package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class LogAnalysisTest {

    @Test
    void 상위_N개_요청_경로를_계산할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.2", 200),
            createAccessLog("/api/users", "192.168.1.3", 200),
            createAccessLog("/api/products", "192.168.1.1", 200),
            createAccessLog("/api/products", "192.168.1.2", 200),
            createAccessLog("/api/orders", "192.168.1.1", 200)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topPaths = logAnalysis.getTopPaths(2);

        // then
        then(topPaths)
            .hasSize(2)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("/api/users", 3L),
                tuple("/api/products", 2L)
            );
    }

    @Test
    void 상위_N개_상태코드를_계산할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.2", 200),
            createAccessLog("/api/users", "192.168.1.3", 200),
            createAccessLog("/api/products", "192.168.1.1", 404),
            createAccessLog("/api/products", "192.168.1.2", 404),
            createAccessLog("/api/orders", "192.168.1.1", 500)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topStatusCodes = logAnalysis.getTopStatusCodes(2);

        // then
        then(topStatusCodes)
            .hasSize(2)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("200", 3L),
                tuple("404", 2L)
            );
    }

    @Test
    void 상위_N개_클라이언트_IP를_계산할_수_있다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/products", "192.168.1.2", 200),
            createAccessLog("/api/products", "192.168.1.2", 200),
            createAccessLog("/api/orders", "192.168.1.3", 200)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topClientIps = logAnalysis.getTopClientIps(2);

        // then
        then(topClientIps)
            .hasSize(2)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("192.168.1.1", 3L),
                tuple("192.168.1.2", 2L)
            );
    }

    @Test
    void 로그가_N개_미만이면_있는_만큼만_반환한다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/products", "192.168.1.2", 200)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topPaths = logAnalysis.getTopPaths(10);

        // then
        then(topPaths).hasSize(2);
    }

    @Test
    void 동일_카운트인_경우_사전순으로_정렬한다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/zebra", "192.168.1.1", 200),
            createAccessLog("/api/apple", "192.168.1.2", 200),
            createAccessLog("/api/banana", "192.168.1.3", 200)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topPaths = logAnalysis.getTopPaths(3);

        // then
        then(topPaths)
            .extracting(RankedItem::value)
            .containsExactly("/api/apple", "/api/banana", "/api/zebra");
    }

    @Test
    void 빈_로그_리스트에서_Top_N을_계산하면_빈_리스트를_반환한다() {
        // given
        LogAnalysis logAnalysis = new LogAnalysis(List.of(), List.of());

        // when
        List<RankedItem> topPaths = logAnalysis.getTopPaths(10);

        // then
        then(topPaths).isEmpty();
    }

    @Test
    void 퍼센티지가_올바르게_계산된다() {
        // given
        List<AccessLog> accessLogs = List.of(
            createAccessLog("/api/users", "192.168.1.1", 200),
            createAccessLog("/api/users", "192.168.1.2", 200),
            createAccessLog("/api/products", "192.168.1.1", 200),
            createAccessLog("/api/orders", "192.168.1.1", 200)
        );
        LogAnalysis logAnalysis = new LogAnalysis(accessLogs, List.of());

        // when
        List<RankedItem> topPaths = logAnalysis.getTopPaths(3);

        // then
        then(topPaths)
            .extracting(RankedItem::value, RankedItem::percentage)
            .containsExactly(
                tuple("/api/users", 50.0),
                tuple("/api/orders", 25.0),
                tuple("/api/products", 25.0)
            );
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
