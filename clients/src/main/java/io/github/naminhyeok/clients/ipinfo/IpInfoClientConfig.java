package io.github.naminhyeok.clients.ipinfo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IpInfoProperties.class)
public class IpInfoClientConfig {

    @Bean
    public IpInfoClient ipInfoClient(IpInfoProperties properties) {
        RestClient restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.token())
            .build();

        return new IpInfoHttpClient(restClient);
    }
}
