package io.github.naminhyeok.core.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class LogAnalysis {

    private final Long id;
    private final List<AccessLog> accessLogs;
    private final List<ParseError> errors;
    private final LocalDateTime analyzedAt;

    public LogAnalysis(List<AccessLog> accessLogs, List<ParseError> errors) {
        this(null, accessLogs, errors, LocalDateTime.now());
    }

    private LogAnalysis(Long id, List<AccessLog> accessLogs, List<ParseError> errors, LocalDateTime analyzedAt) {
        this.id = id;
        this.accessLogs = accessLogs;
        this.errors = errors;
        this.analyzedAt = analyzedAt;
    }

    public LogAnalysis withId(Long id) {
        return new LogAnalysis(id, this.accessLogs, this.errors, this.analyzedAt);
    }

}
