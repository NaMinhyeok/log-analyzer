package io.github.naminhyeok.clients.ipinfo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ipinfo")
public record IpInfoProperties(
    String baseUrl,
    String token
) {
}
