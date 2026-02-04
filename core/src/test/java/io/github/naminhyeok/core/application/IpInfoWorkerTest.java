package io.github.naminhyeok.core.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.support.fake.FakeIpInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

class IpInfoWorkerTest {

    private PendingIpQueue pendingIpQueue;
    private FakeIpInfoClient fakeIpInfoClient;
    private Cache<String, IpInfo> cache;
    private IpInfoWorker ipInfoWorker;

    @BeforeEach
    void setUp() {
        pendingIpQueue = new PendingIpQueue();
        fakeIpInfoClient = new FakeIpInfoClient();
        cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
        ipInfoWorker = new IpInfoWorker(pendingIpQueue, fakeIpInfoClient, cache);
    }

    @Test
    void 큐에_있는_IP를_처리하여_캐시에_저장한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withIpInfo(ip, "US", "California", "Mountain View", "Google LLC");
        pendingIpQueue.offer(ip);

        // when
        ipInfoWorker.start();

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            IpInfo cached = cache.getIfPresent(ip);
            then(cached).isNotNull();
            then(cached.country()).isEqualTo("US");
            then(cached.city()).isEqualTo("Mountain View");
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void IP_조회_실패_시_unknown으로_캐싱한다() {
        // given
        String ip = "192.168.0.1";
        fakeIpInfoClient.withException(new RuntimeException("Network error"));
        pendingIpQueue.offer(ip);

        // when
        ipInfoWorker.start();

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            IpInfo cached = cache.getIfPresent(ip);
            then(cached).isNotNull();
            then(cached.isUnknown()).isTrue();
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void 등록되지_않은_IP는_unknown으로_캐싱된다() {
        // given
        String unknownIp = "10.0.0.1";
        pendingIpQueue.offer(unknownIp);

        // when
        ipInfoWorker.start();

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            IpInfo cached = cache.getIfPresent(unknownIp);
            then(cached).isNotNull();
            then(cached.isUnknown()).isTrue();
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void 이미_캐시된_IP는_클라이언트를_호출하지_않는다() {
        // given
        String ip = "8.8.8.8";
        IpInfo existingInfo = new IpInfo(ip, "US", "California", "Mountain View", "Google LLC");
        cache.put(ip, existingInfo);
        fakeIpInfoClient.withIpInfo(ip, "JP", "Tokyo", "Tokyo", "Other Corp");
        pendingIpQueue.offer(ip);

        // when
        ipInfoWorker.start();

        // then - 잠시 대기 후 클라이언트 호출 확인
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            then(pendingIpQueue.isPending(ip)).isFalse(); // 처리 완료됨
            then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(0); // 클라이언트 미호출
            then(cache.getIfPresent(ip).country()).isEqualTo("US"); // 기존 캐시 유지
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void 처리_완료_후_pendingSet에서_제거된다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withIpInfo(ip, "US", "California", "Mountain View", "Google LLC");
        pendingIpQueue.offer(ip);
        then(pendingIpQueue.isPending(ip)).isTrue();

        // when
        ipInfoWorker.start();

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            then(pendingIpQueue.isPending(ip)).isFalse();
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void 여러_IP를_순차적으로_처리한다() {
        // given
        fakeIpInfoClient
            .withIpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC")
            .withIpInfo("1.1.1.1", "AU", "New South Wales", "Sydney", "Cloudflare");

        pendingIpQueue.offer("8.8.8.8");
        pendingIpQueue.offer("1.1.1.1");

        // when
        ipInfoWorker.start();

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            then(cache.getIfPresent("8.8.8.8")).isNotNull();
            then(cache.getIfPresent("1.1.1.1")).isNotNull();
            then(cache.getIfPresent("8.8.8.8").country()).isEqualTo("US");
            then(cache.getIfPresent("1.1.1.1").country()).isEqualTo("AU");
        });

        // cleanup
        ipInfoWorker.stop();
    }

    @Test
    void null_응답은_unknown으로_캐싱된다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withNullResponse(ip);
        pendingIpQueue.offer(ip);

        // when
        ipInfoWorker.start();

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            IpInfo cached = cache.getIfPresent(ip);
            then(cached).isNotNull();
            then(cached.isUnknown()).isTrue();
        });

        // cleanup
        ipInfoWorker.stop();
    }
}
