package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.core.domain.counter.FrequencyCounter;
import io.github.naminhyeok.core.domain.counter.ParseErrorCollector;
import io.github.naminhyeok.core.domain.counter.StatusCodeCounter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class LogAnalysisAggregateTest {

    @Test
    void withId로_새_ID가_할당된_Aggregate를_생성한다() {
        // given
        LogAnalysisAggregate aggregate = createAggregate();

        // when
        LogAnalysisAggregate withId = aggregate.withId(100L);

        // then
        then(aggregate.getId()).isNull();
        then(withId.getId()).isEqualTo(100L);
    }

    @Test
    void withId는_불변성을_유지한다() {
        // given
        LogAnalysisAggregate original = createAggregate();
        LocalDateTime originalTime = original.getAnalyzedAt();

        // when
        LogAnalysisAggregate withId = original.withId(100L);

        // then
        then(withId.getAnalyzedAt()).isEqualTo(originalTime);
        then(withId.getTotalRequests()).isEqualTo(original.getTotalRequests());
    }

    @Test
    void getTotalRequests는_총_요청_수를_반환한다() {
        // given
        StatusCodeCounter statusCodeCounter = new StatusCodeCounter();
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));
        statusCodeCounter.increment(HttpStatusCode.valueOf(404));

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            new FrequencyCounter(), new FrequencyCounter(),
            statusCodeCounter, new ParseErrorCollector()
        );

        // when & then
        then(aggregate.getTotalRequests()).isEqualTo(3);
    }

    @Test
    void getTopPaths는_상위_경로를_반환한다() {
        // given
        FrequencyCounter pathCounter = new FrequencyCounter();
        pathCounter.increment("/api/users");
        pathCounter.increment("/api/users");
        pathCounter.increment("/api/orders");

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            pathCounter, new FrequencyCounter(),
            new StatusCodeCounter(), new ParseErrorCollector()
        );

        // when
        List<RankedItem> topPaths = aggregate.getTopPaths(2);

        // then
        then(topPaths)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("/api/users", 2L),
                tuple("/api/orders", 1L)
            );
    }

    @Test
    void getTopClientIps는_상위_IP를_반환한다() {
        // given
        FrequencyCounter ipCounter = new FrequencyCounter();
        ipCounter.increment("192.168.0.1");
        ipCounter.increment("192.168.0.1");
        ipCounter.increment("10.0.0.1");

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            new FrequencyCounter(), ipCounter,
            new StatusCodeCounter(), new ParseErrorCollector()
        );

        // when
        List<RankedItem> topIps = aggregate.getTopClientIps(2);

        // then
        then(topIps)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("192.168.0.1", 2L),
                tuple("10.0.0.1", 1L)
            );
    }

    @Test
    void getTopStatusCodes는_상위_상태코드를_반환한다() {
        // given
        StatusCodeCounter statusCodeCounter = new StatusCodeCounter();
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));
        statusCodeCounter.increment(HttpStatusCode.valueOf(404));

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            new FrequencyCounter(), new FrequencyCounter(),
            statusCodeCounter, new ParseErrorCollector()
        );

        // when
        List<RankedItem> topStatusCodes = aggregate.getTopStatusCodes(2);

        // then
        then(topStatusCodes)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("200", 2L),
                tuple("404", 1L)
            );
    }

    @Test
    void getStatusCodeDistribution은_상태코드_분포를_반환한다() {
        // given
        StatusCodeCounter statusCodeCounter = new StatusCodeCounter();
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));
        statusCodeCounter.increment(HttpStatusCode.valueOf(201));
        statusCodeCounter.increment(HttpStatusCode.valueOf(404));
        statusCodeCounter.increment(HttpStatusCode.valueOf(500));

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            new FrequencyCounter(), new FrequencyCounter(),
            statusCodeCounter, new ParseErrorCollector()
        );

        // when
        StatusCodeDistribution distribution = aggregate.getStatusCodeDistribution();

        // then
        then(distribution.successRate()).isEqualTo(50.0);
        then(distribution.clientErrorRate()).isEqualTo(25.0);
        then(distribution.serverErrorRate()).isEqualTo(25.0);
    }

    @Test
    void 파싱_오류_정보를_반환한다() {
        // given
        ParseErrorCollector errorCollector = new ParseErrorCollector();
        errorCollector.add(5, "invalid,line", "파싱 오류");
        errorCollector.add(10, "another,error", "다른 오류");

        LogAnalysisAggregate aggregate = new LogAnalysisAggregate(
            null, LocalDateTime.now(),
            new FrequencyCounter(), new FrequencyCounter(),
            new StatusCodeCounter(), errorCollector
        );

        // when & then
        then(aggregate.getParseErrorCount()).isEqualTo(2);
        then(aggregate.getParseErrorSamples())
            .hasSize(2)
            .extracting(ParseError::lineNumber)
            .containsExactly(5, 10);
    }

    private LogAnalysisAggregate createAggregate() {
        FrequencyCounter pathCounter = new FrequencyCounter();
        pathCounter.increment("/api/users");

        FrequencyCounter ipCounter = new FrequencyCounter();
        ipCounter.increment("1.1.1.1");

        StatusCodeCounter statusCodeCounter = new StatusCodeCounter();
        statusCodeCounter.increment(HttpStatusCode.valueOf(200));

        return new LogAnalysisAggregate(
            null,
            LocalDateTime.now(),
            pathCounter,
            ipCounter,
            statusCodeCounter,
            new ParseErrorCollector()
        );
    }
}
