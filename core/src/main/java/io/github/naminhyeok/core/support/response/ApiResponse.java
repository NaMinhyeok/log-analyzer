package io.github.naminhyeok.core.support.response;

import io.github.naminhyeok.core.support.error.ErrorMessage;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "API 공통 응답 구조")
public class ApiResponse<S> {

    @Schema(description = "결과 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "ERROR"})
    private final ResultType result;

    @Schema(description = "응답 데이터 (성공 시)")
    private final S data;

    @Schema(description = "에러 정보 (실패 시)")
    private final ErrorMessage error;

    private ApiResponse(ResultType result, S data, ErrorMessage error) {
        this.result = result;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<?> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error));
    }

    public static ApiResponse<?> error(ErrorType error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error, errorData));
    }

}
