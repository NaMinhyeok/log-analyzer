package io.github.naminhyeok.core.support.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(IpInfoCacheProperties.class)
public class IpInfoCacheConfig {

    @Bean
    public Cache<String, IpInfo> ipInfoCache(IpInfoCacheProperties properties) {
        return Caffeine.newBuilder()
            .maximumSize(properties.maxSize())
            .expireAfterWrite(Duration.ofMinutes(properties.ttlMinutes()))
            .build();
    }
}
