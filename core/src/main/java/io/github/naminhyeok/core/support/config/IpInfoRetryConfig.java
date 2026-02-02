package io.github.naminhyeok.core.support.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(IpInfoRetryProperties.class)
public class IpInfoRetryConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public Retry ipInfoRetry(IpInfoRetryProperties properties, RetryRegistry retryRegistry) {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(properties.maxAttempts())
            .intervalFunction(intervalFunction(properties))
            .retryOnException(this::isRetryable)
            .build();

        return retryRegistry.retry("ipInfo", config);
    }

    private io.github.resilience4j.core.IntervalFunction intervalFunction(IpInfoRetryProperties properties) {
        return io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
            Duration.ofMillis(properties.waitDurationMs()),
            properties.exponentialBackoffMultiplier()
        );
    }

    private boolean isRetryable(Throwable throwable) {
        // 타임아웃
        if (throwable instanceof SocketTimeoutException) {
            return true;
        }
        // 연결 실패
        if (throwable instanceof ConnectException) {
            return true;
        }
        // Spring RestClient 네트워크 예외 (타임아웃 포함)
        if (throwable instanceof ResourceAccessException) {
            return true;
        }
        // 5xx 서버 에러
        if (throwable instanceof HttpServerErrorException) {
            return true;
        }
        // 429 Too Many Requests (레이트리밋)
        if (throwable instanceof HttpClientErrorException.TooManyRequests) {
            return true;
        }
        // 원인 예외 체크
        if (throwable.getCause() != null) {
            return isRetryable(throwable.getCause());
        }
        return false;
    }
}
