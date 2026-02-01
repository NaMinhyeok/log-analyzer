package io.github.naminhyeok.core.infrastructure.persistence;

import io.github.naminhyeok.core.domain.AccessLog;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.domain.ParseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;

class InMemoryLogAnalysisRepositoryTest {

    private InMemoryLogAnalysisRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLogAnalysisRepository();
    }

    @Test
    void 저장된_분석_결과를_ID로_조회할_수_있다() {
        // given
        AccessLog accessLog = new AccessLog(
            LocalDateTime.of(2026, 1, 29, 10, 30, 0),
            "192.168.1.1",
            HttpMethod.GET,
            "/api/users",
            "Mozilla/5.0",
            HttpStatusCode.valueOf(200),
            "HTTP/1.1",
            100L,
            500L,
            50L,
            "TLSv1.2",
            "/api/users"
        );
        LogAnalysis logAnalysis = new LogAnalysis(List.of(accessLog), List.of());
        LogAnalysis savedLogAnalysis = repository.save(logAnalysis);

        // when
        Optional<LogAnalysis> foundLogAnalysis = repository.findById(savedLogAnalysis.getId());

        // then
        then(foundLogAnalysis).isPresent();
        then(foundLogAnalysis.get().getId()).isEqualTo(savedLogAnalysis.getId());
        then(foundLogAnalysis.get().getAccessLogs()).hasSize(1);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<LogAnalysis> foundLogAnalysis = repository.findById(nonExistentId);

        // then
        then(foundLogAnalysis).isEmpty();
    }
}
