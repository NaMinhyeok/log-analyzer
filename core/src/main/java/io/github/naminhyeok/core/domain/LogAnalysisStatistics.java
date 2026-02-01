package io.github.naminhyeok.core.domain;

import java.util.List;

public record LogAnalysisStatistics(
    long totalRequests,
    StatusCodeDistribution statusCodeDistribution,
    List<RankedItem> topPaths,
    List<RankedItem> topStatusCodes,
    List<RankedItem> topClientIps
) {

    public static LogAnalysisStatistics from(LogAnalysis analysis, int topN) {
        List<AccessLog> accessLogs = analysis.getAccessLogs();

        return new LogAnalysisStatistics(
            accessLogs.size(),
            StatusCodeDistribution.from(accessLogs),
            analysis.getTopPaths(topN),
            analysis.getTopStatusCodes(topN),
            analysis.getTopClientIps(topN)
        );
    }
}
