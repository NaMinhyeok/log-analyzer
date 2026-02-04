package io.github.naminhyeok.core.domain;

import java.util.Optional;

public interface LogAnalysisAggregateRepository {
    LogAnalysisAggregate save(LogAnalysisAggregate aggregate);
    Optional<LogAnalysisAggregate> findById(Long id);
}
