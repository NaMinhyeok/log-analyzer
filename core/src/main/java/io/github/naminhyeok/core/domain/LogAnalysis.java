package io.github.naminhyeok.core.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public List<RankedItem> getTopPaths(int limit) {
        return getTopItems(AccessLog::requestUri, limit);
    }

    public List<RankedItem> getTopStatusCodes(int limit) {
        return getTopItems(log -> String.valueOf(log.httpStatus().value()), limit);
    }

    public List<RankedItem> getTopClientIps(int limit) {
        return getTopItems(AccessLog::clientIp, limit);
    }

    private List<RankedItem> getTopItems(Function<AccessLog, String> keyExtractor, int limit) {
        if (accessLogs.isEmpty()) {
            return List.of();
        }

        long total = accessLogs.size();
        Map<String, Long> countMap = accessLogs.stream()
            .collect(Collectors.groupingBy(keyExtractor, Collectors.counting()));

        return countMap.entrySet().stream()
            .sorted(Comparator
                .comparing(Map.Entry<String, Long>::getValue).reversed()
                .thenComparing(Map.Entry::getKey))
            .limit(limit)
            .map(entry -> new RankedItem(
                entry.getKey(),
                entry.getValue(),
                (double) entry.getValue() / total * 100.0
            ))
            .toList();
    }

}
