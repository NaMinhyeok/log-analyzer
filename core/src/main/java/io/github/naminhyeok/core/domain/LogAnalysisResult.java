package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.clients.ipinfo.IpInfo;

import java.util.Map;

public record LogAnalysisResult(
    LogAnalysisAggregate aggregate,
    Map<String, IpInfo> enrichedIps
) {

    public static LogAnalysisResult of(LogAnalysisAggregate aggregate, Map<String, IpInfo> enrichedIps) {
        return new LogAnalysisResult(aggregate, enrichedIps);
    }

    public IpInfo getIpInfo(String ip) {
        return enrichedIps.getOrDefault(ip, IpInfo.unknown(ip));
    }
}
