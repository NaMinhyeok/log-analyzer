package io.github.naminhyeok.core.application;

import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.infrastructure.persistence.InMemoryLogAnalysisRepository;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class LogAnalysisServiceTest {

    private LogAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new LogAnalysisService(new CsvParser(), new InMemoryLogAnalysisRepository());
    }

    @Test
    void 정상적인_CSV를_파싱하면_AccessLog_목록을_반환한다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysis result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getAccessLogs()).hasSize(1);
        then(result.getErrors()).isEmpty();
        then(result.getAccessLogs())
            .first()
            .extracting(
                AccessLog::clientIp,
                AccessLog::httpMethod,
                AccessLog::httpStatus
            )
            .containsExactly(
                "121.158.115.86",
                HttpMethod.GET,
                HttpStatusCode.valueOf(200)
            );
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
        LogAnalysis result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getAccessLogs()).hasSize(2);
        then(result.getErrors()).hasSize(1);
    }

    @Test
    void 빈_파일은_빈_결과를_반환한다() {
        // given
        String csv = "";
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysis result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getAccessLogs()).isEmpty();
        then(result.getErrors()).isEmpty();
    }

    @Test
    void 헤더만_있는_파일은_빈_결과를_반환한다() {
        // given
        String csv = "header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12";
        MultipartFile file = toMultipartFile(csv);

        // when
        LogAnalysis result = service.analyze(file);

        // then
        then(result.getId()).isNotNull();
        then(result.getAccessLogs()).isEmpty();
        then(result.getErrors()).isEmpty();
    }

    @Test
    void 분석_결과를_ID로_조회할_수_있다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        MultipartFile file = toMultipartFile(csv);
        LogAnalysis savedLogAnalysis = service.analyze(file);

        // when
        LogAnalysis foundLogAnalysis = service.getAnalysis(savedLogAnalysis.getId());

        // then
        then(foundLogAnalysis.getId()).isEqualTo(savedLogAnalysis.getId());
        then(foundLogAnalysis.getAccessLogs()).hasSize(1);
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

    private MultipartFile toMultipartFile(String content) {
        return new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
