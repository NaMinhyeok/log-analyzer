package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LogAnalysisService {

    private final LogAnalyzer logAnalyzer;
    private final LogAnalysisFinder logAnalysisFinder;
    private final LogAnalysisEnricher logAnalysisEnricher;

    public LogAnalysisService(
        LogAnalyzer logAnalyzer,
        LogAnalysisFinder logAnalysisFinder,
        LogAnalysisEnricher logAnalysisEnricher
    ) {
        this.logAnalyzer = logAnalyzer;
        this.logAnalysisFinder = logAnalysisFinder;
        this.logAnalysisEnricher = logAnalysisEnricher;
    }

    public LogAnalysisAggregate analyze(MultipartFile file) {
        return logAnalyzer.analyze(file);
    }

    public LogAnalysisAggregate getAnalysis(Long analysisId) {
        return logAnalysisFinder.find(analysisId);
    }

    public LogAnalysisResult getAnalysisResult(Long analysisId, int topN) {
        LogAnalysisAggregate aggregate = logAnalysisFinder.find(analysisId);
        return logAnalysisEnricher.enrich(aggregate, topN);
    }
}
