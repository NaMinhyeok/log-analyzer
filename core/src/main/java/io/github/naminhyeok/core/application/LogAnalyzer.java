package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisAggregateRepository;
import io.github.naminhyeok.core.domain.LogStreamAggregator;
import io.github.naminhyeok.core.domain.RankedItem;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvParser;
import io.github.naminhyeok.core.support.parser.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LogAnalyzer {

    private static final int TOP_IP_PRELOAD_COUNT = 10;

    private final LogAnalysisAggregateRepository repository;
    private final PendingQueue<String> pendingIpQueue;

    public LogAnalyzer(LogAnalysisAggregateRepository repository, PendingQueue<String> pendingIpQueue) {
        this.repository = repository;
        this.pendingIpQueue = pendingIpQueue;
    }

    public LogAnalysisAggregate analyze(MultipartFile file) {
        log.info("로그 분석 시작: fileName={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        long startTime = System.currentTimeMillis();

        LogStreamAggregator aggregator = new LogStreamAggregator();
        AtomicInteger lineNumber = new AtomicInteger(1);

        try {
            CsvParser.parse(file.getInputStream(), stream ->
                stream.forEach(row -> processRow(row, aggregator, lineNumber))
            );
        } catch (IOException e) {
            throw new CoreException(ErrorType.FILE_READ_ERROR);
        }

        LogAnalysisAggregate aggregate = aggregator.finish();
        LogAnalysisAggregate savedAggregate = repository.save(aggregate);

        preloadTopIps(savedAggregate);
        logCompletion(savedAggregate, startTime);
        return savedAggregate;
    }

    private void preloadTopIps(LogAnalysisAggregate aggregate) {
        List<String> topIps = aggregate.getTopClientIps(TOP_IP_PRELOAD_COUNT)
            .stream()
            .map(RankedItem::value)
            .toList();

        int addedCount = pendingIpQueue.offerAll(topIps);
        log.debug("Top {} IPs queued for preload: {} added", TOP_IP_PRELOAD_COUNT, addedCount);
    }

    private void processRow(CsvRow row, LogStreamAggregator aggregator, AtomicInteger lineNumber) {
        int currentLine = lineNumber.incrementAndGet();
        try {
            AccessLog accessLog = AccessLog.from(row);
            aggregator.accumulate(accessLog);
        } catch (CoreException e) {
            aggregator.recordError(currentLine, toRawLine(row), e.getMessage());
        }
    }

    private void logCompletion(LogAnalysisAggregate aggregate, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("로그 분석 완료: analysisId={}, totalRequests={}, parseErrors={}, elapsedTime={}ms",
            aggregate.getId(), aggregate.getTotalRequests(), aggregate.getParseErrorCount(), elapsedTime);

        if (aggregate.getParseErrorCount() > 0) {
            log.warn("파싱 오류 발생: {} 건", aggregate.getParseErrorCount());
        }
    }

    private String toRawLine(CsvRow row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(row.get(i));
        }
        return sb.toString();
    }
}
