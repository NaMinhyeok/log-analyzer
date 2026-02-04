package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.clients.ipinfo.IpInfoClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IpInfoWorker {

    private final PendingQueue<String> pendingIpQueue;
    private final IpInfoClient ipInfoClient;
    private final Cache<String, IpInfo> cache;
    private volatile boolean running = true;
    private Thread workerThread;

    public IpInfoWorker(
        PendingQueue<String> pendingIpQueue,
        IpInfoClient ipInfoClient,
        Cache<String, IpInfo> cache
    ) {
        this.pendingIpQueue = pendingIpQueue;
        this.ipInfoClient = ipInfoClient;
        this.cache = cache;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        workerThread = Thread.ofVirtual()
            .name("ip-info-worker")
            .start(this::processQueue);
        log.info("IpInfoWorker started (virtual thread)");
    }

    private void processQueue() {
        while (running) {
            try {
                String ip = pendingIpQueue.take();
                processIp(ip);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("IpInfoWorker interrupted");
                break;
            }
        }
    }

    private void processIp(String ip) {
        try {
            IpInfo cached = cache.getIfPresent(ip);
            if (cached != null) {
                log.debug("IP already cached, skipping: {}", ip);
                return;
            }

            IpInfo fetched = ipInfoClient.getIpInfo(ip);
            if (fetched != null) {
                cache.put(ip, fetched);
                log.debug("IP info cached: {} -> {}", ip, fetched.country());
            } else {
                IpInfo unknown = IpInfo.unknown(ip);
                cache.put(ip, unknown);
                log.debug("IP info unknown, cached: {}", ip);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch IP info: {} - {}", ip, e.getMessage());
            IpInfo unknown = IpInfo.unknown(ip);
            cache.put(ip, unknown);
        } finally {
            pendingIpQueue.markCompleted(ip);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        log.info("IpInfoWorker stopped");
    }

}
