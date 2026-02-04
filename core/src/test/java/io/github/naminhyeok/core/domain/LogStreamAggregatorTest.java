package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class LogStreamAggregatorTest {

    @Test
    void 로그를_집계한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();

        // when
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "2.2.2.2", 200));
        aggregator.accumulate(createAccessLog("/api/orders", "1.1.1.1", 404));

        // then
        LogAnalysisAggregate result = aggregator.finish();
        then(result.getTotalRequests()).isEqualTo(3);
    }

    @Test
    void 상위_경로를_집계한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/orders", "1.1.1.1", 200));

        // when
        LogAnalysisAggregate result = aggregator.finish();

        // then
        then(result.getTopPaths(2))
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("/api/users", 2L),
                tuple("/api/orders", 1L)
            );
    }

    @Test
    void 상위_클라이언트_IP를_집계한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "2.2.2.2", 200));

        // when
        LogAnalysisAggregate result = aggregator.finish();

        // then
        then(result.getTopClientIps(2))
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("1.1.1.1", 2L),
                tuple("2.2.2.2", 1L)
            );
    }

    @Test
    void 상위_상태코드를_집계한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 404));

        // when
        LogAnalysisAggregate result = aggregator.finish();

        // then
        then(result.getTopStatusCodes(2))
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("200", 2L),
                tuple("404", 1L)
            );
    }

    @Test
    void 상태코드_분포를_집계한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 201));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 404));
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 500));

        // when
        LogAnalysisAggregate result = aggregator.finish();
        StatusCodeDistribution distribution = result.getStatusCodeDistribution();

        // then
        then(distribution.successRate()).isEqualTo(50.0);
        then(distribution.clientErrorRate()).isEqualTo(25.0);
        then(distribution.serverErrorRate()).isEqualTo(25.0);
    }

    @Test
    void 파싱_오류를_수집한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();

        // when
        aggregator.recordError(5, "invalid,line", "파싱 오류");
        aggregator.recordError(10, "another,error", "다른 오류");

        // then
        LogAnalysisAggregate result = aggregator.finish();
        then(result.getParseErrorCount()).isEqualTo(2);
        then(result.getParseErrorSamples())
            .hasSize(2)
            .extracting(ParseError::lineNumber)
            .containsExactly(5, 10);
    }

    @Test
    void finish는_ID가_null인_Aggregate를_반환한다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));

        // when
        LogAnalysisAggregate result = aggregator.finish();

        // then
        then(result.getId()).isNull();
        then(result.getAnalyzedAt()).isNotNull();
    }

    @Test
    void finish_후에도_aggregator는_계속_사용_가능하다() {
        // given
        LogStreamAggregator aggregator = new LogStreamAggregator();
        aggregator.accumulate(createAccessLog("/api/users", "1.1.1.1", 200));
        LogAnalysisAggregate first = aggregator.finish();

        // when - finish 후 추가 집계
        aggregator.accumulate(createAccessLog("/api/orders", "2.2.2.2", 404));
        LogAnalysisAggregate second = aggregator.finish();

        // then - 첫 번째 결과는 영향받지 않음 (copy로 생성했으므로)
        then(first.getTotalRequests()).isEqualTo(1);
        then(second.getTotalRequests()).isEqualTo(2);
    }

    private AccessLog createAccessLog(String path, String ip, int statusCode) {
        return new AccessLog(
            LocalDateTime.now(),
            ip,
            HttpMethod.GET,
            path,
            "Mozilla/5.0",
            HttpStatusCode.valueOf(statusCode),
            "HTTP/1.1",
            0L,
            0L,
            0L,
            "TLSv1.2",
            path
        );
    }
}
