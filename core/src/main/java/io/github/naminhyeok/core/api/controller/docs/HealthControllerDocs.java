package io.github.naminhyeok.core.api.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "시스템")
public interface HealthControllerDocs {

    @Operation(
        summary = "헬스 체크",
        description = "서버 상태를 확인합니다."
    )
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    Object health();
}
