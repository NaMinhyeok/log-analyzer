package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.clients.ipinfo.IpInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IpInfoReader {

    private final Cache<String, IpInfo> cache;
    private final IpInfoClient ipInfoClient;

    public IpInfoReader(Cache<String, IpInfo> cache, IpInfoClient ipInfoClient) {
        this.cache = cache;
        this.ipInfoClient = ipInfoClient;
    }

    public IpInfo read(String ip) {
        IpInfo cached = cache.getIfPresent(ip);
        if (cached != null) {
            log.debug("Cache HIT for IP: {}", ip);
            return cached;
        }

        log.debug("Cache MISS for IP: {}", ip);
        IpInfo fetched = ipInfoClient.getIpInfo(ip);

        if (fetched == null) {
            log.warn("IpInfoClient returned null for IP: {}", ip);
            return IpInfo.unknown(ip);
        }

        if (!fetched.isUnknown()) {
            cache.put(ip, fetched);
        }
        return fetched;
    }

    public Map<String, IpInfo> readAll(List<String> ips) {
        return ips.stream()
            .distinct()
            .collect(Collectors.toMap(
                Function.identity(),
                this::read
            ));
    }
}
