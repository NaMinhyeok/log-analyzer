package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.ParseError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ParseErrorResponseTest {

    @Test
    void ParseError로부터_ParseErrorResponse를_생성할_수_있다() {
        // given
        ParseError parseError = ParseError.of(42, "invalid,data,here", "파싱 오류 메시지");

        // when
        ParseErrorResponse response = ParseErrorResponse.from(parseError);

        // then
        then(response)
            .extracting(
                ParseErrorResponse::lineNumber,
                ParseErrorResponse::rawLine,
                ParseErrorResponse::errorMessage
            )
            .containsExactly(42, "invalid,data,here", "파싱 오류 메시지");
    }
}
