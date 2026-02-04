package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그 분석 응답")
public record LogAnalysisResponse(
    @Schema(description = "분석 ID (결과 조회 시 사용)", example = "1")
    Long analysisId
) {
    public static LogAnalysisResponse from(LogAnalysisAggregate aggregate) {
        return new LogAnalysisResponse(aggregate.getId());
    }
}
