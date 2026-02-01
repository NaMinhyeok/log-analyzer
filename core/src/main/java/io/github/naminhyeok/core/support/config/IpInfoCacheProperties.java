package io.github.naminhyeok.core.support.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ipinfo.cache")
public record IpInfoCacheProperties(
    Long ttlMinutes,
    Long maxSize
) {
    private static final long DEFAULT_TTL_MINUTES = 60;
    private static final long DEFAULT_MAX_SIZE = 1000;

    public IpInfoCacheProperties {
        if (ttlMinutes == null || ttlMinutes <= 0) {
            ttlMinutes = DEFAULT_TTL_MINUTES;
        }
        if (maxSize == null || maxSize <= 0) {
            maxSize = DEFAULT_MAX_SIZE;
        }
    }
}
