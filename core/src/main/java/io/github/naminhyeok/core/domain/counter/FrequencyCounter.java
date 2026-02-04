package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.RankedItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequencyCounter {

    private final Map<String, Long> counts;
    private long total;

    public FrequencyCounter() {
        this.counts = new HashMap<>();
        this.total = 0;
    }

    private FrequencyCounter(Map<String, Long> counts, long total) {
        this.counts = new HashMap<>(counts);
        this.total = total;
    }

    public void increment(String key) {
        counts.merge(key, 1L, Long::sum);
        total++;
    }

    public long getTotal() {
        return total;
    }

    public List<RankedItem> getTop(int limit) {
        return counts.entrySet().stream()
            .sorted(Comparator
                .comparing(Map.Entry<String, Long>::getValue).reversed()
                .thenComparing(Map.Entry::getKey))
            .limit(limit)
            .map(this::toRankedItem)
            .toList();
    }

    private RankedItem toRankedItem(Map.Entry<String, Long> entry) {
        double percentage = total == 0 ? 0.0 : (double) entry.getValue() / total * 100.0;
        return new RankedItem(entry.getKey(), entry.getValue(), percentage);
    }

    public FrequencyCounter copy() {
        return new FrequencyCounter(this.counts, this.total);
    }
}
