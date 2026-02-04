package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.RankedItem;
import io.github.naminhyeok.core.domain.StatusCodeDistribution;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class StatusCodeCounterTest {

    @Test
    void 상태코드_빈도수를_증가시킬_수_있다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();

        // when
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(404));

        // then
        then(counter.getTotal()).isEqualTo(3);
    }

    @Test
    void 빈도수가_높은_순으로_반환한다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(404));
        counter.increment(HttpStatusCode.valueOf(404));
        counter.increment(HttpStatusCode.valueOf(500));

        // when
        List<RankedItem> top = counter.getTop(3);

        // then
        then(top)
            .hasSize(3)
            .extracting(RankedItem::value, RankedItem::count)
            .containsExactly(
                tuple("200", 3L),
                tuple("404", 2L),
                tuple("500", 1L)
            );
    }

    @Test
    void 동일_빈도수일_때_상태코드_오름차순으로_정렬한다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();
        counter.increment(HttpStatusCode.valueOf(500));
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(404));

        // when
        List<RankedItem> top = counter.getTop(3);

        // then
        then(top)
            .extracting(RankedItem::value)
            .containsExactly("200", "404", "500");
    }

    @Test
    void 카테고리별_분포를_계산한다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();
        counter.increment(HttpStatusCode.valueOf(200));  // 2xx
        counter.increment(HttpStatusCode.valueOf(201));  // 2xx
        counter.increment(HttpStatusCode.valueOf(301));  // 3xx
        counter.increment(HttpStatusCode.valueOf(404));  // 4xx
        counter.increment(HttpStatusCode.valueOf(500));  // 5xx

        // when
        StatusCodeDistribution distribution = counter.getDistribution();

        // then
        then(distribution)
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(40.0, 20.0, 20.0, 20.0);
    }

    @Test
    void 빈_카운터의_분포는_모두_0이다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();

        // when
        StatusCodeDistribution distribution = counter.getDistribution();

        // then
        then(distribution)
            .extracting(
                StatusCodeDistribution::successRate,
                StatusCodeDistribution::redirectRate,
                StatusCodeDistribution::clientErrorRate,
                StatusCodeDistribution::serverErrorRate
            )
            .containsExactly(0.0, 0.0, 0.0, 0.0);
    }

    @Test
    void 빈_카운터에서_getTop은_빈_리스트를_반환한다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();

        // when
        List<RankedItem> top = counter.getTop(5);

        // then
        then(top).isEmpty();
    }

    @Test
    void copy는_독립적인_복사본을_생성한다() {
        // given
        StatusCodeCounter original = new StatusCodeCounter();
        original.increment(HttpStatusCode.valueOf(200));
        original.increment(HttpStatusCode.valueOf(200));

        // when
        StatusCodeCounter copied = original.copy();
        copied.increment(HttpStatusCode.valueOf(500));

        // then
        then(original.getTotal()).isEqualTo(2);
        then(copied.getTotal()).isEqualTo(3);
        then(original.getDistribution().serverErrorRate()).isEqualTo(0.0);
        then(copied.getDistribution().serverErrorRate()).isCloseTo(33.33, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void 퍼센티지를_계산한다() {
        // given
        StatusCodeCounter counter = new StatusCodeCounter();
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(200));
        counter.increment(HttpStatusCode.valueOf(404));
        counter.increment(HttpStatusCode.valueOf(404));

        // when
        List<RankedItem> top = counter.getTop(2);

        // then
        then(top)
            .extracting(RankedItem::percentage)
            .containsExactly(50.0, 50.0);
    }
}
