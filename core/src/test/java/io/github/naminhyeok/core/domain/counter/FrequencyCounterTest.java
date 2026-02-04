package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.RankedItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class FrequencyCounterTest {

    @Test
    void 빈도수를_증가시킬_수_있다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();

        // when
        counter.increment("/api/users");
        counter.increment("/api/users");
        counter.increment("/api/products");

        // then
        then(counter.getTotal()).isEqualTo(3);
    }

    @Test
    void 빈도수가_높은_순으로_반환한다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();
        counter.increment("/api/users");
        counter.increment("/api/users");
        counter.increment("/api/users");
        counter.increment("/api/products");
        counter.increment("/api/products");
        counter.increment("/api/orders");

        // when
        List<RankedItem> top = counter.getTop(3);

        // then
        then(top)
            .hasSize(3)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("/api/users", 3L),
                tuple("/api/products", 2L),
                tuple("/api/orders", 1L)
            );
    }

    @Test
    void 동일_빈도수일_때_알파벳순으로_정렬한다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();
        counter.increment("charlie");
        counter.increment("alpha");
        counter.increment("bravo");

        // when
        List<RankedItem> top = counter.getTop(3);

        // then
        then(top)
            .extracting(RankedItem::value)
            .containsExactly("alpha", "bravo", "charlie");
    }

    @Test
    void 퍼센티지를_계산한다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();
        counter.increment("A");
        counter.increment("A");
        counter.increment("B");
        counter.increment("B");

        // when
        List<RankedItem> top = counter.getTop(2);

        // then
        then(top)
            .extracting(RankedItem::percentage)
            .containsExactly(50.0, 50.0);
    }

    @Test
    void limit보다_적은_항목이_있으면_있는_만큼만_반환한다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();
        counter.increment("only-one");

        // when
        List<RankedItem> top = counter.getTop(10);

        // then
        then(top).hasSize(1);
        then(top.get(0).value()).isEqualTo("only-one");
    }

    @Test
    void 빈_카운터에서_getTop은_빈_리스트를_반환한다() {
        // given
        FrequencyCounter counter = new FrequencyCounter();

        // when
        List<RankedItem> top = counter.getTop(5);

        // then
        then(top).isEmpty();
    }

    @Test
    void copy는_독립적인_복사본을_생성한다() {
        // given
        FrequencyCounter original = new FrequencyCounter();
        original.increment("key1");
        original.increment("key1");

        // when
        FrequencyCounter copied = original.copy();
        copied.increment("key2");

        // then
        then(original.getTotal()).isEqualTo(2);
        then(copied.getTotal()).isEqualTo(3);
    }
}
