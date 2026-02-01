package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisRepository;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class LogAnalysisFinder {

    private final LogAnalysisRepository logAnalysisRepository;

    public LogAnalysisFinder(LogAnalysisRepository logAnalysisRepository) {
        this.logAnalysisRepository = logAnalysisRepository;
    }

    public LogAnalysis findById(Long analysisId) {
        return logAnalysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(ErrorType.ANALYSIS_NOT_FOUND));
    }
}
