package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.RankedItem;
import io.github.naminhyeok.core.domain.StatusCodeDistribution;
import org.springframework.http.HttpStatusCode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusCodeCounter {

    private final Map<Integer, Long> counts;
    private long total;
    private long count2xx;
    private long count3xx;
    private long count4xx;
    private long count5xx;

    public StatusCodeCounter() {
        this.counts = new HashMap<>();
        this.total = 0;
        this.count2xx = 0;
        this.count3xx = 0;
        this.count4xx = 0;
        this.count5xx = 0;
    }

    private StatusCodeCounter(Map<Integer, Long> counts, long total,
                              long count2xx, long count3xx, long count4xx, long count5xx) {
        this.counts = new HashMap<>(counts);
        this.total = total;
        this.count2xx = count2xx;
        this.count3xx = count3xx;
        this.count4xx = count4xx;
        this.count5xx = count5xx;
    }

    public void increment(HttpStatusCode status) {
        counts.merge(status.value(), 1L, Long::sum);
        total++;
        incrementCategoryCount(status);
    }

    private void incrementCategoryCount(HttpStatusCode status) {
        if (status.is2xxSuccessful()) {
            count2xx++;
        } else if (status.is3xxRedirection()) {
            count3xx++;
        } else if (status.is4xxClientError()) {
            count4xx++;
        } else if (status.is5xxServerError()) {
            count5xx++;
        }
    }

    public long getTotal() {
        return total;
    }

    public List<RankedItem> getTop(int limit) {
        return counts.entrySet().stream()
            .sorted(Comparator
                .comparing(Map.Entry<Integer, Long>::getValue).reversed()
                .thenComparing(Map.Entry::getKey))
            .limit(limit)
            .map(this::toRankedItem)
            .toList();
    }

    private RankedItem toRankedItem(Map.Entry<Integer, Long> entry) {
        double percentage = total == 0 ? 0.0 : (double) entry.getValue() / total * 100.0;
        return new RankedItem(String.valueOf(entry.getKey()), entry.getValue(), percentage);
    }

    public StatusCodeDistribution getDistribution() {
        return new StatusCodeDistribution(
            percentage(count2xx),
            percentage(count3xx),
            percentage(count4xx),
            percentage(count5xx)
        );
    }

    private double percentage(long count) {
        return total == 0 ? 0.0 : (double) count / total * 100.0;
    }

    public StatusCodeCounter copy() {
        return new StatusCodeCounter(
            this.counts, this.total,
            this.count2xx, this.count3xx, this.count4xx, this.count5xx
        );
    }
}
