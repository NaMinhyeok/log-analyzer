package io.github.naminhyeok.core.domain;

public record RankedItem(
    String value,
    long count,
    double percentage
) {
}
