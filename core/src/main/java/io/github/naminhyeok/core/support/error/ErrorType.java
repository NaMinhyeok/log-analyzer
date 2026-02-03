package io.github.naminhyeok.core.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, ErrorCode.E400, "요청이 올바르지 않습니다.",
        LogLevel.INFO),
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.",
        LogLevel.ERROR),

    // 파싱 관련 에러 (1000번대)
    PARSE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, ErrorCode.E1000, "요청 데이터 형식이 올바르지 않습니다.",
        LogLevel.INFO),
    FILE_READ_ERROR(HttpStatus.UNPROCESSABLE_CONTENT, ErrorCode.E1001, "파일을 처리할 수 없습니다.",
        LogLevel.WARN),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.E1002, "파일 크기가 제한을 초과했습니다.",
        LogLevel.INFO),

    // 분석 관련 에러 (2000번대)
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E2000, "분석 결과를 찾을 수 없습니다.",
        LogLevel.INFO);

    private final HttpStatus status;

    private final ErrorCode code;

    private final String message;

    private final LogLevel logLevel;

    ErrorType(HttpStatus status, ErrorCode code, String message, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

}
