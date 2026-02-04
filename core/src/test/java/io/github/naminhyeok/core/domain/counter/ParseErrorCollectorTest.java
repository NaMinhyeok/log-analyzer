package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.ParseError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class ParseErrorCollectorTest {

    @Test
    void 파싱_오류를_추가할_수_있다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();

        // when
        collector.add(1, "invalid,line", "파싱 오류");
        collector.add(5, "another,error", "다른 오류");

        // then
        then(collector.getTotalCount()).isEqualTo(2);
        then(collector.getSamples()).hasSize(2);
    }

    @Test
    void 최대_10개까지만_샘플을_저장한다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();

        // when
        for (int i = 1; i <= 15; i++) {
            collector.add(i, "line" + i, "error" + i);
        }

        // then
        then(collector.getTotalCount()).isEqualTo(15);
        then(collector.getSamples()).hasSize(10);
    }

    @Test
    void 샘플은_먼저_추가된_순서대로_저장된다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();

        // when
        collector.add(1, "first", "첫 번째");
        collector.add(2, "second", "두 번째");
        collector.add(3, "third", "세 번째");

        // then
        List<ParseError> samples = collector.getSamples();
        then(samples.get(0).lineNumber()).isEqualTo(1);
        then(samples.get(1).lineNumber()).isEqualTo(2);
        then(samples.get(2).lineNumber()).isEqualTo(3);
    }

    @Test
    void getSamples는_불변_리스트를_반환한다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();
        collector.add(1, "line", "error");

        // when
        List<ParseError> samples = collector.getSamples();

        // then
        org.junit.jupiter.api.Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> samples.add(ParseError.of(2, "new", "new"))
        );
    }

    @Test
    void 빈_collector에서_getSamples는_빈_리스트를_반환한다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();

        // when
        List<ParseError> samples = collector.getSamples();

        // then
        then(samples).isEmpty();
    }

    @Test
    void copy는_독립적인_복사본을_생성한다() {
        // given
        ParseErrorCollector original = new ParseErrorCollector();
        original.add(1, "line1", "error1");

        // when
        ParseErrorCollector copied = original.copy();
        copied.add(2, "line2", "error2");

        // then
        then(original.getTotalCount()).isEqualTo(1);
        then(copied.getTotalCount()).isEqualTo(2);
        then(original.getSamples()).hasSize(1);
        then(copied.getSamples()).hasSize(2);
    }

    @Test
    void 샘플이_가득_찬_후에도_totalCount는_증가한다() {
        // given
        ParseErrorCollector collector = new ParseErrorCollector();
        for (int i = 1; i <= 10; i++) {
            collector.add(i, "line" + i, "error" + i);
        }

        // when
        collector.add(11, "line11", "error11");
        collector.add(12, "line12", "error12");

        // then
        then(collector.getTotalCount()).isEqualTo(12);
        then(collector.getSamples()).hasSize(10);
        // 처음 10개만 저장되어 있음
        then(collector.getSamples().get(9).lineNumber()).isEqualTo(10);
    }
}
