package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.LogAnalysis;

public record LogAnalysisResponse(
    Long analysisId
) {
    public static LogAnalysisResponse from(LogAnalysis logAnalysis) {
        return new LogAnalysisResponse(logAnalysis.getId());
    }
}
