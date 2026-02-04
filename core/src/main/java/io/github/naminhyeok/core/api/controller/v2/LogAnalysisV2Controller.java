package io.github.naminhyeok.core.api.controller.v2;

import io.github.naminhyeok.core.api.controller.docs.LogAnalysisControllerV2Docs;
import io.github.naminhyeok.core.api.controller.v1.response.LogAnalysisResponse;
import io.github.naminhyeok.core.application.LogAnalysisService;
import io.github.naminhyeok.core.domain.LogAnalysisAggregate;
import io.github.naminhyeok.core.support.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/logs")
public class LogAnalysisV2Controller implements LogAnalysisControllerV2Docs {

    private final LogAnalysisService logAnalysisService;

    public LogAnalysisV2Controller(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @Override
    @PostMapping(value = "/v2/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LogAnalysisResponse>> analyze(
        @RequestPart("file") MultipartFile file
    ) {
        LogAnalysisAggregate aggregate = logAnalysisService.analyze(file);
        URI location = URI.create("/api/logs/v1/analysis/" + aggregate.getId());

        return ResponseEntity
            .accepted()
            .location(location)
            .body(ApiResponse.success(LogAnalysisResponse.from(aggregate)));
    }
}
