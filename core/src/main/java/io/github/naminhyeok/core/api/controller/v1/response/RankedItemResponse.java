package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.core.domain.RankedItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "순위 항목")
public record RankedItemResponse(
    @Schema(description = "항목 값", example = "/api/users")
    String value,
    @Schema(description = "요청 횟수", example = "1500")
    long count,
    @Schema(description = "전체 대비 비율 (%)", example = "10.5")
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
