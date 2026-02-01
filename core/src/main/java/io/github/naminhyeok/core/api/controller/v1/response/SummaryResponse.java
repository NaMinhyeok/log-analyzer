package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysisStatistics;

public record SummaryResponse(
    long totalRequests,
    StatusDistributionResponse statusCodeDistribution
) {

    public static SummaryResponse from(LogAnalysisStatistics statistics) {
        return new SummaryResponse(
            statistics.totalRequests(),
            StatusDistributionResponse.from(statistics.statusCodeDistribution())
        );
    }
}
