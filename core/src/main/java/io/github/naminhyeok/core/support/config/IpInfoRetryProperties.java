package io.github.naminhyeok.core.support.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ipinfo.retry")
public record IpInfoRetryProperties(
    Integer maxAttempts,
    Long waitDurationMs,
    Double exponentialBackoffMultiplier
) {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_WAIT_DURATION_MS = 500;
    private static final double DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0;

    public IpInfoRetryProperties {
        if (maxAttempts == null || maxAttempts <= 0) {
            maxAttempts = DEFAULT_MAX_ATTEMPTS;
        }
        if (waitDurationMs == null || waitDurationMs <= 0) {
            waitDurationMs = DEFAULT_WAIT_DURATION_MS;
        }
        if (exponentialBackoffMultiplier == null || exponentialBackoffMultiplier <= 1.0) {
            exponentialBackoffMultiplier = DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER;
        }
    }
}
