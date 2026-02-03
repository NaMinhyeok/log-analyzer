package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.ParseError;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파싱 오류 정보")
public record ParseErrorResponse(
    @Schema(description = "오류 발생 라인 번호", example = "42")
    int lineNumber,
    @Schema(description = "원본 라인 내용", example = "invalid,csv,format")
    String rawLine,
    @Schema(description = "오류 메시지", example = "필드 개수가 부족합니다")
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
