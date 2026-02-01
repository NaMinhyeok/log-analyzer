package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.domain.LogAnalysisStatistics;
import io.github.naminhyeok.core.domain.RankedItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LogAnalysisEnricher {

    private final IpInfoReader ipInfoReader;

    public LogAnalysisEnricher(IpInfoReader ipInfoReader) {
        this.ipInfoReader = ipInfoReader;
    }

    public LogAnalysisResult enrich(LogAnalysis logAnalysis, int topN) {
        List<String> topClientIps = extractTopClientIps(logAnalysis, topN);
        Map<String, IpInfo> enrichedIps = ipInfoReader.readAll(topClientIps);

        return LogAnalysisResult.of(logAnalysis, enrichedIps);
    }

    private List<String> extractTopClientIps(LogAnalysis logAnalysis, int topN) {
        LogAnalysisStatistics statistics = logAnalysis.calculateStatistics(topN);
        return statistics.topClientIps().stream()
            .map(RankedItem::value)
            .toList();
    }
}
