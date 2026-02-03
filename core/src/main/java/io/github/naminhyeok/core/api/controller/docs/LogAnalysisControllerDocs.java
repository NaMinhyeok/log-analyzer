package io.github.naminhyeok.core.api.controller.docs;

import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResponse;
import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResultResponse;
import io.github.naminhyeok.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "로그 분석")
public interface LogAnalysisControllerDocs {

    @Operation(
        summary = "로그 파일 분석",
        description = "IIS 로그 파일(CSV)을 업로드하여 분석을 시작합니다. 분석 완료 후 결과 조회에 사용할 analysisId를 반환합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "분석 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 데이터 형식 오류 (E1000)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "E1000",
                            "message": "요청 데이터 형식이 올바르지 않습니다.",
                            "data": null
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "파일 크기 초과 (E1002)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "E1002",
                            "message": "파일 크기가 제한을 초과했습니다.",
                            "data": null
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "파일 처리 불가 (E1001)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "E1001",
                            "message": "파일을 처리할 수 없습니다.",
                            "data": null
                          }
                        }
                        """
                )
            )
        )
    })
    ApiResponse<LogAnalysisResponse> analyze(
        @Parameter(description = "분석할 IIS 로그 파일 (CSV 형식)", required = true)
        MultipartFile file
    );

    @Operation(
        summary = "분석 결과 조회",
        description = "분석 ID로 상세 분석 결과를 조회합니다. topN 파라미터로 상위 항목 개수를 지정할 수 있습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (E400) - topN이 1 미만인 경우",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "E400",
                            "message": "요청이 올바르지 않습니다.",
                            "data": "topN은 1 이상이어야 합니다."
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "분석 결과 없음 (E2000)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "E2000",
                            "message": "분석 결과를 찾을 수 없습니다.",
                            "data": null
                          }
                        }
                        """
                )
            )
        )
    })
    ApiResponse<LogAnalysisResultResponse> getAnalysis(
        @Parameter(description = "분석 ID", required = true, example = "1")
        Long analysisId,
        @Parameter(description = "상위 항목 개수 (기본값: 10, 최소값: 1)", example = "10")
        int topN
    );
}
