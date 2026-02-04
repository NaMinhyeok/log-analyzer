package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.core.domain.counter.FrequencyCounter;
import io.github.naminhyeok.core.domain.counter.ParseErrorCollector;
import io.github.naminhyeok.core.domain.counter.StatusCodeCounter;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class LogAnalysisAggregate {

    private final Long id;
    private final LocalDateTime analyzedAt;
    private final FrequencyCounter pathCounter;
    private final FrequencyCounter ipCounter;
    private final StatusCodeCounter statusCodeCounter;
    private final ParseErrorCollector errorCollector;

    public LogAnalysisAggregate(
        Long id,
        LocalDateTime analyzedAt,
        FrequencyCounter pathCounter,
        FrequencyCounter ipCounter,
        StatusCodeCounter statusCodeCounter,
        ParseErrorCollector errorCollector
    ) {
        this.id = id;
        this.analyzedAt = analyzedAt;
        this.pathCounter = pathCounter;
        this.ipCounter = ipCounter;
        this.statusCodeCounter = statusCodeCounter;
        this.errorCollector = errorCollector;
    }

    public LogAnalysisAggregate withId(Long id) {
        return new LogAnalysisAggregate(
            id,
            this.analyzedAt,
            this.pathCounter,
            this.ipCounter,
            this.statusCodeCounter,
            this.errorCollector
        );
    }

    public long getTotalRequests() {
        return statusCodeCounter.getTotal();
    }

    public List<RankedItem> getTopPaths(int topN) {
        return pathCounter.getTop(topN);
    }

    public List<RankedItem> getTopClientIps(int topN) {
        return ipCounter.getTop(topN);
    }

    public List<RankedItem> getTopStatusCodes(int topN) {
        return statusCodeCounter.getTop(topN);
    }

    public StatusCodeDistribution getStatusCodeDistribution() {
        return statusCodeCounter.getDistribution();
    }

    public int getParseErrorCount() {
        return errorCollector.getTotalCount();
    }

    public List<ParseError> getParseErrorSamples() {
        return errorCollector.getSamples();
    }
}
