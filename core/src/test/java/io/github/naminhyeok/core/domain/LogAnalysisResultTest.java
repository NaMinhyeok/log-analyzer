package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

class LogAnalysisResultTest {

    @Test
    void LogAnalysis와_IP_정보로_LogAnalysisResult를_생성할_수_있다() {
        // given
        LogAnalysis logAnalysis = new LogAnalysis(List.of(), List.of());
        Map<String, IpInfo> enrichedIps = Map.of(
            "8.8.8.8", new IpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
        );

        // when
        LogAnalysisResult result = LogAnalysisResult.of(logAnalysis, enrichedIps);

        // then
        then(result.logAnalysis()).isEqualTo(logAnalysis);
        then(result.enrichedIps()).isEqualTo(enrichedIps);
    }

    @Test
    void 존재하는_IP의_정보를_조회할_수_있다() {
        // given
        LogAnalysis logAnalysis = new LogAnalysis(List.of(), List.of());
        IpInfo googleIpInfo = new IpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC");
        LogAnalysisResult result = LogAnalysisResult.of(logAnalysis, Map.of("8.8.8.8", googleIpInfo));

        // when
        IpInfo ipInfo = result.getIpInfo("8.8.8.8");

        // then
        then(ipInfo).isEqualTo(googleIpInfo);
    }

    @Test
    void 존재하지_않는_IP는_unknown_정보를_반환한다() {
        // given
        LogAnalysis logAnalysis = new LogAnalysis(List.of(), List.of());
        LogAnalysisResult result = LogAnalysisResult.of(logAnalysis, Map.of());

        // when
        IpInfo ipInfo = result.getIpInfo("192.168.0.1");

        // then
        then(ipInfo.isUnknown()).isTrue();
        then(ipInfo.ip()).isEqualTo("192.168.0.1");
    }
}
