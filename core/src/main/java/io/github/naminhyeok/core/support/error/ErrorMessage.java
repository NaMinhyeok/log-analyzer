package io.github.naminhyeok.core.support.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "에러 상세 정보")
public class ErrorMessage {

    @Schema(description = "에러 코드", example = "E2000")
    private final String code;

    @Schema(description = "에러 메시지", example = "분석 결과를 찾을 수 없습니다.")
    private final String message;

    @Schema(description = "추가 데이터 (선택)")
    private final Object data;

    public ErrorMessage(ErrorType errorType) {
        this.code = errorType.getCode().name();
        this.message = errorType.getMessage();
        this.data = null;
    }

    public ErrorMessage(ErrorType errorType, Object data) {
        this.code = errorType.getCode().name();
        this.message = errorType.getMessage();
        this.data = data;
    }

}
