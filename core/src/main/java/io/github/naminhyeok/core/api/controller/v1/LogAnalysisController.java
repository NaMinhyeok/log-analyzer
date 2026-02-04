package io.github.naminhyeok.core.api.controller.v1;

import io.github.naminhyeok.core.api.controller.docs.LogAnalysisControllerDocs;
import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResponse;
import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResultResponse;
import io.github.naminhyeok.core.application.LogAnalysisService;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.domain.LogAnalysisResult;
import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;
import io.github.naminhyeok.core.support.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/logs")
public class LogAnalysisController implements LogAnalysisControllerDocs {

    private final LogAnalysisService logAnalysisService;

    public LogAnalysisController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @Override
    @PostMapping(value = "/v1/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<LogAnalysisResponse> analyze(
        @RequestPart("file") MultipartFile file
    ) {
        LogAnalysisAggregate aggregate = logAnalysisService.analyze(file);
        return ApiResponse.success(LogAnalysisResponse.from(aggregate));
    }

    @Override
    @GetMapping("/v1/analysis/{analysisId}")
    public ApiResponse<LogAnalysisResultResponse> getAnalysis(
        @PathVariable Long analysisId,
        @RequestParam(defaultValue = "10") int topN
    ) {
        if (topN < 1) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "topN은 1 이상이어야 합니다.");
        }
        LogAnalysisResult result = logAnalysisService.getAnalysisResult(analysisId, topN);
        return ApiResponse.success(LogAnalysisResultResponse.from(result, topN));
    }
}
