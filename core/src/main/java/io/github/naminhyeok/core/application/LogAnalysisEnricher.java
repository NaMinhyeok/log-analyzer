package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.domain.RankedItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LogAnalysisEnricher {

    private final IpInfoReader ipInfoReader;

    public LogAnalysisEnricher(IpInfoReader ipInfoReader) {
        this.ipInfoReader = ipInfoReader;
    }

    public LogAnalysisResult enrich(LogAnalysisAggregate aggregate, int topN) {
        log.info("IP 정보 조회 시작: analysisId={}, topN={}", aggregate.getId(), topN);
        long startTime = System.currentTimeMillis();

        List<String> topClientIps = aggregate.getTopClientIps(topN).stream()
            .map(RankedItem::value)
            .toList();

        Map<String, IpInfo> enrichedIps = ipInfoReader.readAll(topClientIps);

        long unknownCount = enrichedIps.values().stream()
            .filter(IpInfo::isUnknown)
            .count();
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("IP 정보 조회 완료: totalIps={}, unknownIps={}, elapsedTime={}ms",
            enrichedIps.size(), unknownCount, elapsedTime);

        return LogAnalysisResult.of(aggregate, enrichedIps);
    }
}
