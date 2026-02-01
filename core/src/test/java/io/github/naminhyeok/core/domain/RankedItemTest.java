package io.github.naminhyeok.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RankedItemTest {

    @Test
    void 값과_카운트_퍼센티지로_RankedItem을_생성할_수_있다() {
        // given
        String value = "/api/users";
        long count = 50L;
        double percentage = 25.0;

        // when
        RankedItem rankedItem = new RankedItem(value, count, percentage);

        // then
        then(rankedItem)
            .extracting(
                RankedItem::value,
                RankedItem::count,
                RankedItem::percentage
            )
            .containsExactly(value, count, percentage);
    }
}
