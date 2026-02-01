package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ParseErrorTest {

    @Test
    void 파싱_오류를_생성할_수_있다() {
        // given
        int lineNumber = 5;
        String rawLine = "invalid,csv,line";
        String errorMessage = "컬럼 수가 부족합니다";

        // when
        ParseError parseError = ParseError.of(lineNumber, rawLine, errorMessage);

        // then
        then(parseError)
            .extracting(ParseError::lineNumber, ParseError::rawLine, ParseError::errorMessage)
            .containsExactly(lineNumber, rawLine, errorMessage);
    }
}
