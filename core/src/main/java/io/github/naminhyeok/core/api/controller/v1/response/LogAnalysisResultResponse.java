package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.domain.LogAnalysisStatistics;

import java.time.LocalDateTime;
import java.util.List;

public record LogAnalysisResultResponse(
    Long analysisId,
    LocalDateTime analyzedAt,
    SummaryResponse summary,
    List<RankedItemResponse> topPaths,
    List<RankedItemResponse> topStatusCodes,
    List<RankedIpResponse> topClientIps,
    int parseErrorCount
) {

    public static LogAnalysisResultResponse from(LogAnalysisResult result, int topN) {
        LogAnalysis analysis = result.logAnalysis();
        LogAnalysisStatistics statistics = analysis.calculateStatistics(topN);

        List<RankedIpResponse> topClientIpsWithDetail = statistics.topClientIps().stream()
            .map(rankedItem -> RankedIpResponse.from(rankedItem, result.getIpInfo(rankedItem.value())))
            .toList();

        return new LogAnalysisResultResponse(
            analysis.getId(),
            analysis.getAnalyzedAt(),
            SummaryResponse.from(statistics),
            statistics.topPaths().stream().map(RankedItemResponse::from).toList(),
            statistics.topStatusCodes().stream().map(RankedItemResponse::from).toList(),
            topClientIpsWithDetail,
            analysis.getErrors().size()
        );
    }
}
