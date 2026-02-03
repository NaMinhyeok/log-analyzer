package io.github.naminhyeok.core.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Log Analyzer API")
                .version("v1.0.0")
                .description("IIS 로그 분석 시스템 API 문서"))
            .tags(List.of(
                new Tag().name("시스템").description("시스템 상태 확인 API"),
                new Tag().name("로그 분석").description("로그 파일 분석 및 결과 조회 API")
            ));
    }
}
