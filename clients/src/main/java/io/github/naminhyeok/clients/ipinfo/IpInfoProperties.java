package io.github.naminhyeok.clients.ipinfo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ipinfo")
public record IpInfoProperties(
    String baseUrl,
    String token,
    Integer connectTimeoutMs,
    Integer readTimeoutMs
) {
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 3000;

    public IpInfoProperties {
        if (connectTimeoutMs == null || connectTimeoutMs <= 0) {
            connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        }
        if (readTimeoutMs == null || readTimeoutMs <= 0) {
            readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
        }
    }
}
