package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisRepository;
import io.github.naminhyeok.core.domain.ParseError;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvParser;
import io.github.naminhyeok.core.support.parser.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LogAnalyzer {

    private final CsvParser csvParser;
    private final LogAnalysisRepository logAnalysisRepository;

    public LogAnalyzer(CsvParser csvParser, LogAnalysisRepository logAnalysisRepository) {
        this.csvParser = csvParser;
        this.logAnalysisRepository = logAnalysisRepository;
    }

    public LogAnalysis analyze(MultipartFile file) {
        log.info("로그 분석 시작: fileName={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        long startTime = System.currentTimeMillis();

        List<AccessLog> parsedAccessLogs = new ArrayList<>();
        List<ParseError> collectedErrors = new ArrayList<>();
        AtomicInteger lineNumber = new AtomicInteger(1);

        try {
            csvParser.parse(file.getInputStream(), stream ->
                stream.forEach(row -> {
                    int currentLine = lineNumber.incrementAndGet();
                    try {
                        AccessLog parsedAccessLog = AccessLog.from(row);
                        parsedAccessLogs.add(parsedAccessLog);
                    } catch (CoreException e) {
                        collectedErrors.add(ParseError.of(currentLine, toRawLine(row), e.getMessage()));
                    }
                })
            );
        } catch (IOException e) {
            throw new CoreException(ErrorType.FILE_READ_ERROR);
        }

        LogAnalysis logAnalysis = new LogAnalysis(parsedAccessLogs, collectedErrors);
        LogAnalysis savedLogAnalysis = logAnalysisRepository.save(logAnalysis);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("로그 분석 완료: analysisId={}, totalRequests={}, parseErrors={}, elapsedTime={}ms",
            savedLogAnalysis.getId(), parsedAccessLogs.size(), collectedErrors.size(), elapsedTime);

        if (!collectedErrors.isEmpty()) {
            log.warn("파싱 오류 발생: {} 건", collectedErrors.size());
        }

        return savedLogAnalysis;
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
