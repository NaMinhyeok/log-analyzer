package io.github.naminhyeok.core.domain;

import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.parser.CsvRow;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public record AccessLog(
    LocalDateTime timeGenerated,
    String clientIp,
    HttpMethod httpMethod,
    String requestUri,
    String userAgent,
    HttpStatusCode httpStatus,
    String httpVersion,
    long receivedBytes,
    long sentBytes,
    long clientResponseTime,
    String sslProtocol,
    String originalRequestUriWithArgs
) {
    private static final int EXPECTED_COLUMN_COUNT = 12;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "M/d/yyyy, h:mm:ss.SSS a", Locale.ENGLISH
    );
    private static final List<HttpMethod> VALID_HTTP_METHODS = List.of(
        HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
        HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.TRACE
    );

    public static AccessLog from(CsvRow row) {
        if (row.size() != EXPECTED_COLUMN_COUNT) {
            throw new CoreException(ErrorType.PARSE_INVALID_REQUEST);
        }

        return new AccessLog(
            parseDateTime(row.get(0)),
            row.get(1),
            parseHttpMethod(row.get(2)),
            row.get(3),
            row.get(4),
            parseHttpStatus(row.get(5)),
            row.get(6),
            parseLong(row.get(7)),
            parseLong(row.get(8)),
            parseLong(row.get(9)),
            row.get(10),
            row.get(11)
        );
    }

    private static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.PARSE_INVALID_REQUEST);
        }
    }

    private static HttpMethod parseHttpMethod(String value) {
        return VALID_HTTP_METHODS.stream()
            .filter(m -> m.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new CoreException(ErrorType.PARSE_INVALID_REQUEST));
    }

    private static HttpStatusCode parseHttpStatus(String value) {
        try {
            return HttpStatusCode.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new CoreException(ErrorType.PARSE_INVALID_REQUEST);
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new CoreException(ErrorType.PARSE_INVALID_REQUEST);
        }
    }
}
