package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvRow;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class AccessLogTest {

    @Test
    void 액세스_로그를_생성할_수_있다() {
        // given
        LocalDateTime timeGenerated = LocalDateTime.of(2026, 1, 29, 5, 44, 10);
        String clientIp = "121.158.115.86";
        HttpMethod httpMethod = HttpMethod.GET;
        String requestUri = "/event/banner/mir2/popup";
        String userAgent = "MyThreadedApp/1.0";
        HttpStatusCode httpStatus = HttpStatusCode.valueOf(200);
        String httpVersion = "HTTP/1.1";
        long receivedBytes = 176L;
        long sentBytes = 1138L;
        long clientResponseTime = 0L;
        String sslProtocol = "TLSv1.2";
        String originalRequestUriWithArgs = "/event/banner/mir2/popup";

        // when
        AccessLog accessLog = new AccessLog(
            timeGenerated,
            clientIp,
            httpMethod,
            requestUri,
            userAgent,
            httpStatus,
            httpVersion,
            receivedBytes,
            sentBytes,
            clientResponseTime,
            sslProtocol,
            originalRequestUriWithArgs
        );

        // then
        then(accessLog)
            .extracting(
                AccessLog::timeGenerated,
                AccessLog::clientIp,
                AccessLog::httpMethod,
                AccessLog::requestUri,
                AccessLog::httpStatus
            )
            .containsExactly(
                timeGenerated,
                clientIp,
                httpMethod,
                requestUri,
                httpStatus
            );
    }

    @Test
    void CsvRow로부터_액세스_로그를_생성할_수_있다() {
        // given
        CsvRow row = new CsvRow(new String[]{
            "1/29/2026, 5:44:10.000 AM",
            "121.158.115.86",
            "GET",
            "/event/banner/mir2/popup",
            "MyThreadedApp/1.0",
            "200",
            "HTTP/1.1",
            "176",
            "1138",
            "0",
            "TLSv1.2",
            "/event/banner/mir2/popup"
        });

        // when
        AccessLog accessLog = AccessLog.from(row);

        // then
        then(accessLog)
            .extracting(
                AccessLog::timeGenerated,
                AccessLog::clientIp,
                AccessLog::httpMethod,
                AccessLog::requestUri,
                AccessLog::httpStatus
            )
            .containsExactly(
                LocalDateTime.of(2026, 1, 29, 5, 44, 10),
                "121.158.115.86",
                HttpMethod.GET,
                "/event/banner/mir2/popup",
                HttpStatusCode.valueOf(200)
            );
    }

    @Test
    void 컬럼_수가_부족하면_예외가_발생한다() {
        // given
        CsvRow row = new CsvRow(new String[]{"value1", "value2", "value3"});

        // when & then
        thenThrownBy(() -> AccessLog.from(row))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException coreException = (CoreException) e;
                then(coreException.getErrorType()).isEqualTo(ErrorType.PARSE_INVALID_REQUEST);
            });
    }

    @Test
    void 잘못된_날짜_형식이면_예외가_발생한다() {
        // given
        CsvRow row = new CsvRow(new String[]{
            "invalid-date",
            "121.158.115.86",
            "GET",
            "/event/banner",
            "MyApp/1.0",
            "200",
            "HTTP/1.1",
            "176",
            "1138",
            "0",
            "TLSv1.2",
            "/event/banner"
        });

        // when & then
        thenThrownBy(() -> AccessLog.from(row))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException coreException = (CoreException) e;
                then(coreException.getErrorType()).isEqualTo(ErrorType.PARSE_INVALID_REQUEST);
            });
    }

    @Test
    void 잘못된_숫자_형식이면_예외가_발생한다() {
        // given
        CsvRow row = new CsvRow(new String[]{
            "1/29/2026, 5:44:10.000 AM",
            "121.158.115.86",
            "GET",
            "/event/banner",
            "MyApp/1.0",
            "not-a-number",
            "HTTP/1.1",
            "176",
            "1138",
            "0",
            "TLSv1.2",
            "/event/banner"
        });

        // when & then
        thenThrownBy(() -> AccessLog.from(row))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException coreException = (CoreException) e;
                then(coreException.getErrorType()).isEqualTo(ErrorType.PARSE_INVALID_REQUEST);
            });
    }

    @Test
    void 잘못된_HTTP_메서드면_예외가_발생한다() {
        // given
        CsvRow row = new CsvRow(new String[]{
            "1/29/2026, 5:44:10.000 AM",
            "121.158.115.86",
            "INVALID_METHOD",
            "/event/banner",
            "MyApp/1.0",
            "200",
            "HTTP/1.1",
            "176",
            "1138",
            "0",
            "TLSv1.2",
            "/event/banner"
        });

        // when & then
        thenThrownBy(() -> AccessLog.from(row))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException coreException = (CoreException) e;
                then(coreException.getErrorType()).isEqualTo(ErrorType.PARSE_INVALID_REQUEST);
            });
    }
}
