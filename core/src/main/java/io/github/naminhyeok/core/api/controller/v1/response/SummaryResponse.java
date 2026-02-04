package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "분석 요약 정보")
public record SummaryResponse(
    @Schema(description = "총 요청 수", example = "15000")
    long totalRequests,
    @Schema(description = "HTTP 상태 코드 분포")
    StatusDistributionResponse statusCodeDistribution
) {

    public static SummaryResponse from(LogAnalysisAggregate aggregate) {
        return new SummaryResponse(
            aggregate.getTotalRequests(),
            StatusDistributionResponse.from(aggregate.getStatusCodeDistribution())
        );
    }
}
