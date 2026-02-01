package io.github.naminhyeok.core.domain;

import java.util.List;

public record StatusCodeDistribution(
    double successRate,
    double redirectRate,
    double clientErrorRate,
    double serverErrorRate
) {

    public static StatusCodeDistribution from(List<AccessLog> accessLogs) {
        if (accessLogs.isEmpty()) {
            return new StatusCodeDistribution(0.0, 0.0, 0.0, 0.0);
        }

        long total = accessLogs.size();
        long successCount = accessLogs.stream()
            .filter(log -> log.httpStatus().is2xxSuccessful())
            .count();
        long redirectCount = accessLogs.stream()
            .filter(log -> log.httpStatus().is3xxRedirection())
            .count();
        long clientErrorCount = accessLogs.stream()
            .filter(log -> log.httpStatus().is4xxClientError())
            .count();
        long serverErrorCount = accessLogs.stream()
            .filter(log -> log.httpStatus().is5xxServerError())
            .count();

        return new StatusCodeDistribution(
            calculatePercentage(successCount, total),
            calculatePercentage(redirectCount, total),
            calculatePercentage(clientErrorCount, total),
            calculatePercentage(serverErrorCount, total)
        );
    }

    private static double calculatePercentage(long count, long total) {
        return (double) count / total * 100.0;
    }
}
