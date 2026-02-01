package io.github.naminhyeok.core.domain;

import java.util.Optional;

public interface LogAnalysisRepository {

    LogAnalysis save(LogAnalysis logAnalysis);

    Optional<LogAnalysis> findById(Long id);

}
