package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.infrastructure.persistence.InMemoryLogAnalysisAggregateRepository;
import io.github.naminhyeok.core.support.fake.FakePendingIpQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class LogAnalyzerTest {

    private LogAnalyzer logAnalyzer;
    private FakePendingIpQueue fakePendingIpQueue;

    @BeforeEach
    void setUp() {
        fakePendingIpQueue = new FakePendingIpQueue();
        logAnalyzer = new LogAnalyzer(
            new InMemoryLogAnalysisAggregateRepository(),
            fakePendingIpQueue
        );
    }

    @Test
    void 분석_완료_후_top_10_IP가_큐에_적재된다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",192.168.0.1,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            "1/29/2026, 5:44:10.000 AM",192.168.0.1,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            "1/29/2026, 5:44:10.000 AM",192.168.0.2,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            "1/29/2026, 5:44:10.000 AM",192.168.0.3,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysisAggregate result = logAnalyzer.analyze(file);

        // then
        then(result.getId()).isNotNull();
        List<String> offeredIps = fakePendingIpQueue.getOfferedIps();
        then(offeredIps).contains("192.168.0.1", "192.168.0.2", "192.168.0.3");
    }

    @Test
    void top_10개_이하의_IP만_큐에_적재된다() {
        // given
        StringBuilder csv = new StringBuilder();
        csv.append("header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12\n");

        // 15개의 서로 다른 IP 생성
        for (int i = 1; i <= 15; i++) {
            for (int j = 0; j < 16 - i; j++) { // IP별로 다른 요청 횟수 (1위 IP가 가장 많음)
                csv.append(String.format(
                    "\"1/29/2026, 5:44:10.000 AM\",192.168.0.%d,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test%n",
                    i
                ));
            }
        }
        MultipartFile file = toMultipartFile(csv.toString());

        // when
        logAnalyzer.analyze(file);

        // then
        List<String> offeredIps = fakePendingIpQueue.getOfferedIps();
        then(offeredIps).hasSize(10);
    }

    @Test
    void IP가_없는_로그는_빈_큐_적재를_수행한다() {
        // given
        String csv = "header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12\n";
        MultipartFile file = toMultipartFile(csv);

        // when
        logAnalyzer.analyze(file);

        // then
        then(fakePendingIpQueue.getTotalOfferCount()).isZero();
    }

    @Test
    void 중복_IP는_한_번만_큐에_적재된다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",192.168.0.1,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            "1/29/2026, 5:44:10.000 AM",192.168.0.1,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            "1/29/2026, 5:44:10.000 AM",192.168.0.1,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);

        // when
        logAnalyzer.analyze(file);

        // then
        then(fakePendingIpQueue.getOfferCount("192.168.0.1")).isEqualTo(1);
    }

    private MultipartFile toMultipartFile(String content) {
        return new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
