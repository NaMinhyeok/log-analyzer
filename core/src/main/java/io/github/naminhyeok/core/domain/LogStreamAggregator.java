package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.core.domain.counter.FrequencyCounter;
import io.github.naminhyeok.core.domain.counter.ParseErrorCollector;
import io.github.naminhyeok.core.domain.counter.StatusCodeCounter;

import java.time.LocalDateTime;

public class LogStreamAggregator {

    private final FrequencyCounter pathCounter = new FrequencyCounter();
    private final FrequencyCounter ipCounter = new FrequencyCounter();
    private final StatusCodeCounter statusCodeCounter = new StatusCodeCounter();
    private final ParseErrorCollector errorCollector = new ParseErrorCollector();

    public void accumulate(AccessLog log) {
        pathCounter.increment(log.requestUri());
        ipCounter.increment(log.clientIp());
        statusCodeCounter.increment(log.httpStatus());
    }

    public void recordError(int lineNumber, String rawLine, String message) {
        errorCollector.add(lineNumber, rawLine, message);
    }

    public LogAnalysisAggregate finish() {
        return new LogAnalysisAggregate(
            null,
            LocalDateTime.now(),
            pathCounter.copy(),
            ipCounter.copy(),
            statusCodeCounter.copy(),
            errorCollector.copy()
        );
    }
}
