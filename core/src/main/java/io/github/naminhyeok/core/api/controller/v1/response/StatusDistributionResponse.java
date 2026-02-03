package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.StatusCodeDistribution;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HTTP 상태 코드 분포 (백분율)")
public record StatusDistributionResponse(
    @Schema(description = "2xx 성공 비율", example = "85.5")
    double successRate,
    @Schema(description = "3xx 리다이렉트 비율", example = "5.2")
    double redirectRate,
    @Schema(description = "4xx 클라이언트 에러 비율", example = "7.8")
    double clientErrorRate,
    @Schema(description = "5xx 서버 에러 비율", example = "1.5")
    double serverErrorRate
) {

    public static StatusDistributionResponse from(StatusCodeDistribution distribution) {
        return new StatusDistributionResponse(
            distribution.successRate(),
            distribution.redirectRate(),
            distribution.clientErrorRate(),
            distribution.serverErrorRate()
        );
    }
}
