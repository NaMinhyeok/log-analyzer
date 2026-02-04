package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisAggregateRepository;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.infrastructure.persistence.InMemoryLogAnalysisAggregateRepository;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.fake.FakePendingIpQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class LogAnalysisServiceTest {

    private LogAnalysisService service;

    @BeforeEach
    void setUp() {
        LogAnalysisAggregateRepository repository = new InMemoryLogAnalysisAggregateRepository();
        FakePendingIpQueue fakePendingIpQueue = new FakePendingIpQueue();
        Cache<String, IpInfo> cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

        LogAnalyzer logAnalyzer = new LogAnalyzer(repository, fakePendingIpQueue);
        LogAnalysisFinder logAnalysisFinder = new LogAnalysisFinder(repository);
        IpInfoReader ipInfoReader = new IpInfoReader(cache, fakePendingIpQueue);
        LogAnalysisEnricher logAnalysisEnricher = new LogAnalysisEnricher(ipInfoReader);

        service = new LogAnalysisService(logAnalyzer, logAnalysisFinder, logAnalysisEnricher);
    }

    @Test
    void 정상적인_CSV를_파싱하면_집계된_결과를_반환한다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysisAggregate result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getTotalRequests()).isEqualTo(1);
        then(result.getParseErrorCount()).isZero();
        then(result.getTopClientIps(10))
            .hasSize(1)
            .first()
            .satisfies(rankedItem -> {
                then(rankedItem.value()).isEqualTo("121.158.115.86");
                then(rankedItem.count()).isEqualTo(1);
            });
    }

    @Test
    void 파싱_실패한_행은_오류로_수집된다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            invalid,row,data
            "1/29/2026, 5:44:11.000 AM",121.158.115.87,POST,/api/users,Mozilla/5.0,201,HTTP/1.1,150,300,60,TLSv1.2,/api/users
            """;
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysisAggregate result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getTotalRequests()).isEqualTo(2);
        then(result.getParseErrorCount()).isEqualTo(1);
    }

    @Test
    void 빈_파일은_빈_결과를_반환한다() {
        // given
        String csv = "";
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysisAggregate result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getTotalRequests()).isZero();
        then(result.getParseErrorCount()).isZero();
    }

    @Test
    void 헤더만_있는_파일은_빈_결과를_반환한다() {
        // given
        String csv = "header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12";
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysisAggregate result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getTotalRequests()).isZero();
        then(result.getParseErrorCount()).isZero();
    }

    @Test
    void 분석_결과를_ID로_조회할_수_있다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);
        LogAnalysisAggregate savedAggregate = service.analyze(file);

        // when
        LogAnalysisAggregate foundAggregate = service.getAnalysis(savedAggregate.getId());

        // then
        then(foundAggregate.getId()).isEqualTo(savedAggregate.getId());
        then(foundAggregate.getTotalRequests()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_분석_결과를_조회하면_예외가_발생한다() {
        // given
        Long nonExistentId = 999L;

        // when & then
        thenThrownBy(() -> service.getAnalysis(nonExistentId))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException coreException = (CoreException) e;
                then(coreException.getErrorType()).isEqualTo(ErrorType.ANALYSIS_NOT_FOUND);
            });
    }

    @Test
    void IP_정보가_포함된_분석_결과를_조회할_수_있다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);
        LogAnalysisAggregate savedAggregate = service.analyze(file);

        // when
        LogAnalysisResult result = service.getAnalysisResult(savedAggregate.getId(), 10);

        // then
        then(result.aggregate().getId()).isEqualTo(savedAggregate.getId());
        then(result.enrichedIps()).isNotNull();
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
