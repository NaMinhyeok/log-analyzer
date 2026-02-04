package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisAggregateRepository;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class LogAnalysisFinder {

    private final LogAnalysisAggregateRepository repository;

    public LogAnalysisFinder(LogAnalysisAggregateRepository repository) {
        this.repository = repository;
    }

    public LogAnalysisAggregate find(Long analysisId) {
        return repository.findById(analysisId)
            .orElseThrow(() -> new CoreException(ErrorType.ANALYSIS_NOT_FOUND));
    }
}
