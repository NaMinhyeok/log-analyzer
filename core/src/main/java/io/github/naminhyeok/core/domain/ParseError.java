package io.github.naminhyeok.core.domain;

public record ParseError(
    int lineNumber,
    String rawLine,
    String errorMessage
) {
    public static ParseError of(int lineNumber, String rawLine, String errorMessage) {
        return new ParseError(lineNumber, rawLine, errorMessage);
    }
}
