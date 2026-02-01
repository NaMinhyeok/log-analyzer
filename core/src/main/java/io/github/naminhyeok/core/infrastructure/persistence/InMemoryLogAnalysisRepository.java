package io.github.naminhyeok.core.infrastructure.persistence;

import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.LogAnalysisRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLogAnalysisRepository implements LogAnalysisRepository {

    private final Map<Long, LogAnalysis> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public LogAnalysis save(LogAnalysis logAnalysis) {
        LogAnalysis savedLogAnalysis = logAnalysis;
        if (logAnalysis.getId() == null) {
            savedLogAnalysis = logAnalysis.withId(idGenerator.incrementAndGet());
        }
        store.put(savedLogAnalysis.getId(), savedLogAnalysis);
        return savedLogAnalysis;
    }

}
