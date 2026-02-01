package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.RankedItem;

public record RankedItemResponse(
    String value,
    long count,
    double percentage
) {

    public static RankedItemResponse from(RankedItem rankedItem) {
        return new RankedItemResponse(
            rankedItem.value(),
            rankedItem.count(),
            rankedItem.percentage()
        );
    }
}
