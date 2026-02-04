package io.github.naminhyeok.core.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class PendingIpQueue implements PendingQueue<String> {

    private final Set<String> pendingSet = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public boolean offer(String ip) {
        if (!pendingSet.add(ip)) {
            log.debug("IP already pending: {}", ip);
            return false;
        }

        queue.offer(ip);
        log.debug("IP added to pending queue: {}", ip);
        return true;
    }

    public int offerAll(List<String> ips) {
        int addedCount = 0;
        for (String ip : ips) {
            if (offer(ip)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    public String take() throws InterruptedException {
        return queue.take();
    }

    public void markCompleted(String ip) {
        pendingSet.remove(ip);
        log.debug("IP marked completed: {}", ip);
    }

    public int size() {
        return queue.size();
    }

    public boolean isPending(String ip) {
        return pendingSet.contains(ip);
    }
}
