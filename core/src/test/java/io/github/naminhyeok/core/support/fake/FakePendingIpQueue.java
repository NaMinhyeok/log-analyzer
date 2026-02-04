package io.github.naminhyeok.core.support.fake;

import io.github.naminhyeok.core.application.PendingQueue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class FakePendingIpQueue implements PendingQueue<String> {

    private final Set<String> pendingSet = new HashSet<>();
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final List<String> offerHistory = new ArrayList<>();

    @Override
    public boolean offer(String ip) {
        offerHistory.add(ip);
        if (!pendingSet.add(ip)) {
            return false;
        }
        queue.offer(ip);
        return true;
    }

    @Override
    public int offerAll(List<String> ips) {
        int addedCount = 0;
        for (String ip : ips) {
            if (offer(ip)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    @Override
    public String take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void markCompleted(String ip) {
        pendingSet.remove(ip);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isPending(String ip) {
        return pendingSet.contains(ip);
    }

    // 테스트 헬퍼 메서드

    public List<String> getOfferedIps() {
        return List.copyOf(pendingSet);
    }

    public List<String> getOfferHistory() {
        return List.copyOf(offerHistory);
    }

    public int getOfferCount(String ip) {
        return (int) offerHistory.stream().filter(ip::equals).count();
    }

    public int getTotalOfferCount() {
        return offerHistory.size();
    }

    public boolean wasOffered(String ip) {
        return offerHistory.contains(ip);
    }

    public void reset() {
        pendingSet.clear();
        queue.clear();
        offerHistory.clear();
    }
}
