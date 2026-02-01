package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisRepository;
import io.github.naminhyeok.core.domain.ParseError;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvParser;
import io.github.naminhyeok.core.support.parser.CsvRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class LogAnalysisService {

    private final CsvParser csvParser;
    private final LogAnalysisRepository logAnalysisRepository;

    public LogAnalysis getAnalysis(Long analysisId) {
        return logAnalysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(ErrorType.ANALYSIS_NOT_FOUND));
    }

    public LogAnalysis analyze(MultipartFile file) {
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
        return logAnalysisRepository.save(logAnalysis);
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
