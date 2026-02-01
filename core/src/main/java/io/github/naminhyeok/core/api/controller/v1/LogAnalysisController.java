package io.github.naminhyeok.core.api.controller.v1;

import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResponse;
import io.github.naminhyeok.core.application.LogAnalysisService;
import io.github.naminhyeok.core.domain.LogAnalysis;
import io.github.naminhyeok.core.support.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/logs")
public class LogAnalysisController {

    private final LogAnalysisService logAnalysisService;

    public LogAnalysisController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LogAnalysisResponse>> analyze(
        @RequestPart("file") MultipartFile file
    ) {
        LogAnalysis logAnalysis = logAnalysisService.analyze(file);
        return ResponseEntity.ok(ApiResponse.success(LogAnalysisResponse.from(logAnalysis)));
    }
}
