package io.github.naminhyeok.core.api.controller.docs;

import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResponse;
import io.github.naminhyeok.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "로그 분석 v2", description = "비동기 IP 조회를 지원하는 로그 분석 API")
public interface LogAnalysisControllerV2Docs {

    @Operation(
        summary = "로그 파일 분석 (비동기)",
        description = """
            IIS 로그 파일(CSV)을 업로드하여 분석을 시작합니다.

            **v1과의 차이점:**
            - 202 Accepted 응답과 함께 Location 헤더로 결과 조회 URL 제공
            - IP 정보는 백그라운드에서 비동기적으로 조회됨
            - 첫 번째 GET 요청 시 일부 IP가 'UNKNOWN'일 수 있으며, 이후 요청에서 점진적으로 채워짐
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "분석 요청 접수됨",
            headers = @Header(
                name = "Location",
                description = "분석 결과 조회 URL",
                example = "/api/logs/v1/analysis/1"
            )
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
    ResponseEntity<ApiResponse<LogAnalysisResponse>> analyze(
        @Parameter(description = "분석할 IIS 로그 파일 (CSV 형식)", required = true)
        MultipartFile file
    );
}
