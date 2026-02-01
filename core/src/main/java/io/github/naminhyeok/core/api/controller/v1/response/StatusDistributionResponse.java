package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.StatusCodeDistribution;

public record StatusDistributionResponse(
    double successRate,
    double redirectRate,
    double clientErrorRate,
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
