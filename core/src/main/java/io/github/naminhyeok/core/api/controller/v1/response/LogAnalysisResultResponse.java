package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "분석 결과 상세 응답")
public record LogAnalysisResultResponse(
    @Schema(description = "분석 ID", example = "1")
    Long analysisId,
    @Schema(description = "분석 시각", example = "2024-01-15T10:30:00")
    LocalDateTime analyzedAt,
    @Schema(description = "요약 정보")
    SummaryResponse summary,
    @Schema(description = "상위 요청 경로 목록")
    List<RankedItemResponse> topPaths,
    @Schema(description = "상위 HTTP 상태 코드 목록")
    List<RankedItemResponse> topStatusCodes,
    @Schema(description = "상위 클라이언트 IP 목록 (IP 상세 정보 포함)")
    List<RankedIpResponse> topClientIps,
    @Schema(description = "파싱 오류 총 개수", example = "5")
    int parseErrorCount,
    @Schema(description = "파싱 오류 샘플 (최대 10개)")
    List<ParseErrorResponse> parseErrorSamples
) {

    public static LogAnalysisResultResponse from(LogAnalysisResult result, int topN) {
        LogAnalysisAggregate aggregate = result.aggregate();

        List<RankedIpResponse> topClientIpsWithDetail = aggregate.getTopClientIps(topN).stream()
            .map(rankedItem -> RankedIpResponse.from(rankedItem, result.getIpInfo(rankedItem.value())))
            .toList();

        return new LogAnalysisResultResponse(
            aggregate.getId(),
            aggregate.getAnalyzedAt(),
            SummaryResponse.from(aggregate),
            aggregate.getTopPaths(topN).stream().map(RankedItemResponse::from).toList(),
            aggregate.getTopStatusCodes(topN).stream().map(RankedItemResponse::from).toList(),
            topClientIpsWithDetail,
            aggregate.getParseErrorCount(),
            aggregate.getParseErrorSamples().stream().map(ParseErrorResponse::from).toList()
        );
    }
}
