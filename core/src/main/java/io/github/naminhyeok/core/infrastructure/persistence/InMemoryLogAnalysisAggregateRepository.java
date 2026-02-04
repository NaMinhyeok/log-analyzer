package io.github.naminhyeok.core.infrastructure.persistence;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisAggregateRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLogAnalysisAggregateRepository implements LogAnalysisAggregateRepository {

    private final Map<Long, LogAnalysisAggregate> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public LogAnalysisAggregate save(LogAnalysisAggregate aggregate) {
        LogAnalysisAggregate toSave = aggregate;
        if (aggregate.getId() == null) {
            toSave = aggregate.withId(idGenerator.incrementAndGet());
        }
        store.put(toSave.getId(), toSave);
        return toSave;
    }

    @Override
    public Optional<LogAnalysisAggregate> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
