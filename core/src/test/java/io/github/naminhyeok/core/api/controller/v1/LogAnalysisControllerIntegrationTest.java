package io.github.naminhyeok.core.api.controller.v1;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
class LogAnalysisControllerIntegrationTest {

    // application-test.yml의 max-file-size와 동기화 (1KB = 1024 bytes)
    private static final int TEST_MAX_FILE_SIZE_BYTES = 1024;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void 파일_크기가_제한을_초과하면_413_에러를_반환한다() {
        // given - 제한(1KB)을 1바이트 초과하는 파일 생성
        byte[] largeContent = new byte[TEST_MAX_FILE_SIZE_BYTES + 1];
        ByteArrayResource fileResource = new ByteArrayResource(largeContent) {
            @Override
            public String getFilename() {
                return "large-test.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        // when & then
        restTestClient.post()
            .uri("/api/logs/v1/analyze")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .exchange()
            .expectStatus().isEqualTo(413)
            .expectBody()
            .jsonPath("$.result").isEqualTo("ERROR")
            .jsonPath("$.error.code").isEqualTo("E1002")
            .jsonPath("$.error.message").isEqualTo("파일 크기가 제한을 초과했습니다.");
    }

    @Test
    void 정상_크기_파일은_업로드할_수_있다() {
        // given
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        ByteArrayResource fileResource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        // when & then
        restTestClient.post()
            .uri("/api/logs/v1/analyze")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.result").isEqualTo("SUCCESS")
            .jsonPath("$.data.analysisId").exists();
    }

    @Test
    void topN이_1보다_작으면_400_에러를_반환한다() {
        // given - 먼저 분석 결과 생성
        String csv = """
            header1,header2,header3,header4,header5,header6,header7,header8,header9,header10,header11,header12
            "1/29/2026, 5:44:10.000 AM",121.158.115.86,GET,/api/test,Mozilla/5.0,200,HTTP/1.1,100,200,50,TLSv1.2,/api/test
            """;
        ByteArrayResource fileResource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        byte[] responseBody = restTestClient.post()
            .uri("/api/logs/v1/analyze")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .returnResult()
            .getResponseBody();

        Integer analysisId = JsonPath.read(new String(responseBody, StandardCharsets.UTF_8), "$.data.analysisId");

        // when & then - topN=0으로 조회
        restTestClient.get()
            .uri("/api/logs/v1/analysis/{analysisId}?topN=0", analysisId)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.result").isEqualTo("ERROR")
            .jsonPath("$.error.code").isEqualTo("E400")
            .jsonPath("$.error.message").isEqualTo("요청이 올바르지 않습니다.");
    }
}
