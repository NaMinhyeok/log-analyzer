package io.github.naminhyeok.core.infrastructure.persistence;

import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.counter.FrequencyCounter;
import io.github.naminhyeok.core.domain.counter.ParseErrorCollector;
import io.github.naminhyeok.core.domain.counter.StatusCodeCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;

class InMemoryLogAnalysisAggregateRepositoryTest {

    private InMemoryLogAnalysisAggregateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLogAnalysisAggregateRepository();
    }

    @Test
    void ID가_null인_Aggregate를_저장하면_ID가_자동_할당된다() {
        // given
        LogAnalysisAggregate aggregate = createAggregate(null);

        // when
        LogAnalysisAggregate saved = repository.save(aggregate);

        // then
        then(saved.getId()).isNotNull();
        then(saved.getId()).isEqualTo(1L);
    }

    @Test
    void 저장된_Aggregate를_ID로_조회할_수_있다() {
        // given
        LogAnalysisAggregate aggregate = createAggregate(null);
        LogAnalysisAggregate saved = repository.save(aggregate);

        // when
        Optional<LogAnalysisAggregate> found = repository.findById(saved.getId());

        // then
        then(found).isPresent();
        then(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
        // when
        Optional<LogAnalysisAggregate> found = repository.findById(999L);

        // then
        then(found).isEmpty();
    }

    @Test
    void 여러_Aggregate를_저장하면_각각_다른_ID가_할당된다() {
        // given
        LogAnalysisAggregate aggregate1 = createAggregate(null);
        LogAnalysisAggregate aggregate2 = createAggregate(null);

        // when
        LogAnalysisAggregate saved1 = repository.save(aggregate1);
        LogAnalysisAggregate saved2 = repository.save(aggregate2);

        // then
        then(saved1.getId()).isEqualTo(1L);
        then(saved2.getId()).isEqualTo(2L);
    }

    @Test
    void ID가_이미_있는_Aggregate를_저장하면_해당_ID로_저장된다() {
        // given
        LogAnalysisAggregate aggregate = createAggregate(100L);

        // when
        LogAnalysisAggregate saved = repository.save(aggregate);

        // then
        then(saved.getId()).isEqualTo(100L);
        then(repository.findById(100L)).isPresent();
    }

    private LogAnalysisAggregate createAggregate(Long id) {
        return new LogAnalysisAggregate(
            id,
            LocalDateTime.now(),
            new FrequencyCounter(),
            new FrequencyCounter(),
            new StatusCodeCounter(),
            new ParseErrorCollector()
        );
    }
}
