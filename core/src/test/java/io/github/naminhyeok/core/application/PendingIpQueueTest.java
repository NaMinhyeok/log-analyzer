package io.github.naminhyeok.core.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.BDDAssertions.then;

class PendingIpQueueTest {

    private PendingIpQueue pendingIpQueue;

    @BeforeEach
    void setUp() {
        pendingIpQueue = new PendingIpQueue();
    }

    @Test
    void IP를_큐에_추가할_수_있다() {
        // given
        String ip = "8.8.8.8";

        // when
        boolean result = pendingIpQueue.offer(ip);

        // then
        then(result).isTrue();
        then(pendingIpQueue.size()).isEqualTo(1);
        then(pendingIpQueue.isPending(ip)).isTrue();
    }

    @Test
    void 중복된_IP는_추가되지_않는다() {
        // given
        String ip = "8.8.8.8";
        pendingIpQueue.offer(ip);

        // when
        boolean result = pendingIpQueue.offer(ip);

        // then
        then(result).isFalse();
        then(pendingIpQueue.size()).isEqualTo(1);
    }

    @Test
    void 여러_IP를_한_번에_추가할_수_있다() {
        // given
        List<String> ips = List.of("8.8.8.8", "1.1.1.1", "192.168.0.1");

        // when
        int addedCount = pendingIpQueue.offerAll(ips);

        // then
        then(addedCount).isEqualTo(3);
        then(pendingIpQueue.size()).isEqualTo(3);
    }

    @Test
    void 중복_IP가_포함된_리스트에서는_유니크한_IP만_추가된다() {
        // given
        List<String> ipsWithDuplicates = List.of("8.8.8.8", "8.8.8.8", "1.1.1.1");

        // when
        int addedCount = pendingIpQueue.offerAll(ipsWithDuplicates);

        // then
        then(addedCount).isEqualTo(2);
        then(pendingIpQueue.size()).isEqualTo(2);
    }

    @Test
    void 큐에서_IP를_꺼낼_수_있다() throws InterruptedException {
        // given
        String ip = "8.8.8.8";
        pendingIpQueue.offer(ip);

        // when
        CountDownLatch latch = new CountDownLatch(1);
        String[] taken = new String[1];

        Thread consumer = new Thread(() -> {
            try {
                taken[0] = pendingIpQueue.take();
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();

        // then
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        then(completed).isTrue();
        then(taken[0]).isEqualTo(ip);
    }

    @Test
    void markCompleted_후_같은_IP를_다시_추가할_수_있다() {
        // given
        String ip = "8.8.8.8";
        pendingIpQueue.offer(ip);
        then(pendingIpQueue.isPending(ip)).isTrue();

        // when
        pendingIpQueue.markCompleted(ip);

        // then
        then(pendingIpQueue.isPending(ip)).isFalse();

        // when - 다시 추가
        boolean result = pendingIpQueue.offer(ip);

        // then
        then(result).isTrue();
        then(pendingIpQueue.isPending(ip)).isTrue();
    }

    @Test
    void 이미_pending인_IP도_markCompleted_후에는_재추가_가능하다() {
        // given
        String ip = "8.8.8.8";
        pendingIpQueue.offer(ip);

        // when - 먼저 완료 처리
        pendingIpQueue.markCompleted(ip);

        // then - 재추가 가능
        boolean reAdded = pendingIpQueue.offer(ip);
        then(reAdded).isTrue();
    }
}
