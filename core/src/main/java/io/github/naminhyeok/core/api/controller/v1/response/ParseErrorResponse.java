package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.ParseError;

public record ParseErrorResponse(
    int lineNumber,
    String rawLine,
    String errorMessage
) {
    public static ParseErrorResponse from(ParseError error) {
        return new ParseErrorResponse(
            error.lineNumber(),
            error.rawLine(),
            error.errorMessage()
        );
    }
}
