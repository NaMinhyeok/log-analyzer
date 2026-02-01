package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisStatistics;

import java.time.LocalDateTime;
import java.util.List;

public record LogAnalysisResultResponse(
    Long analysisId,
    LocalDateTime analyzedAt,
    SummaryResponse summary,
    List<RankedItemResponse> topPaths,
    List<RankedItemResponse> topStatusCodes,
    List<RankedItemResponse> topClientIps,
    int parseErrorCount
) {

    public static LogAnalysisResultResponse from(LogAnalysis analysis, int topN) {
        LogAnalysisStatistics statistics = analysis.calculateStatistics(topN);

        return new LogAnalysisResultResponse(
            analysis.getId(),
            analysis.getAnalyzedAt(),
            SummaryResponse.from(statistics),
            statistics.topPaths().stream().map(RankedItemResponse::from).toList(),
            statistics.topStatusCodes().stream().map(RankedItemResponse::from).toList(),
            statistics.topClientIps().stream().map(RankedItemResponse::from).toList(),
            analysis.getErrors().size()
        );
    }
}
