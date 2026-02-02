package io.github.naminhyeok.clients.ipinfo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(IpInfoProperties.class)
public class IpInfoClientConfig {

    @Bean
    public IpInfoClient ipInfoHttpClient(IpInfoProperties properties) {
        RestClient restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.token())
            .requestFactory(createRequestFactory(properties))
            .build();

        return new IpInfoHttpClient(restClient);
    }

    private ClientHttpRequestFactory createRequestFactory(IpInfoProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        return factory;
    }
}
