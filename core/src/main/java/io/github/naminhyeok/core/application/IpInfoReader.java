package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
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
    private final PendingQueue<String> pendingIpQueue;

    public IpInfoReader(Cache<String, IpInfo> cache, PendingQueue<String> pendingIpQueue) {
        this.cache = cache;
        this.pendingIpQueue = pendingIpQueue;
    }

    public IpInfo read(String ip) {
        IpInfo cached = cache.getIfPresent(ip);
        if (cached != null) {
            log.debug("Cache HIT for IP: {}", ip);
            return cached;
        }

        log.debug("Cache MISS for IP: {}, queueing for async fetch", ip);
        pendingIpQueue.offer(ip);
        return IpInfo.unknown(ip);
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
