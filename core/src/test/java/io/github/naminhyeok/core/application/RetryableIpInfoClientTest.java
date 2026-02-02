package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.support.fake.FakeIpInfoClient;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.*;

class RetryableIpInfoClientTest {

    private FakeIpInfoClient fakeIpInfoClient;
    private Retry retry;
    private RetryableIpInfoClient retryableIpInfoClient;

    @BeforeEach
    void setUp() {
        fakeIpInfoClient = new FakeIpInfoClient();

        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(10)) // 테스트에서는 빠르게
            .retryOnException(e ->
                e instanceof SocketTimeoutException ||
                e instanceof ResourceAccessException ||
                e instanceof HttpServerErrorException ||
                e instanceof HttpClientErrorException.TooManyRequests
            )
            .build();

        retry = Retry.of("ipInfoTest", retryConfig);
        retryableIpInfoClient = new RetryableIpInfoClient(fakeIpInfoClient, retry);
    }

    @Test
    void 첫_번째_시도에서_성공하면_재시도하지_않는다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withIpInfo(ip, "US", "California", "Mountain View", "Google LLC");

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.country()).isEqualTo("US");
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(1);
    }

    @Test
    void 타임아웃_발생_시_재시도한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient
            .withIpInfo(ip, "US", "California", "Mountain View", "Google LLC")
            .failFirstNAttempts(2, new ResourceAccessException("timeout",
                new SocketTimeoutException("Read timed out")));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.country()).isEqualTo("US");
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(3); // 2번 실패 + 1번 성공
    }

    @Test
    void 서버_에러_5xx_발생_시_재시도한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient
            .withIpInfo(ip, "US", "California", "Mountain View", "Google LLC")
            .failFirstNAttempts(1, new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.country()).isEqualTo("US");
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(2); // 1번 실패 + 1번 성공
    }

    @Test
    void 레이트리밋_429_발생_시_재시도한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient
            .withIpInfo(ip, "US", "California", "Mountain View", "Google LLC")
            .failFirstNAttempts(1, HttpClientErrorException.create(
                TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.country()).isEqualTo("US");
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(2);
    }

    @Test
    void 모든_재시도_실패_시_unknown을_반환한다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withException(new ResourceAccessException("Connection refused"));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.isUnknown()).isTrue();
        then(result.ip()).isEqualTo(ip);
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(3); // maxAttempts
    }

    @Test
    void 클라이언트_에러_4xx는_재시도하지_않는다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withException(new HttpClientErrorException(NOT_FOUND));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.isUnknown()).isTrue();
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(1); // 재시도 없이 바로 실패
    }

    @Test
    void 인증_에러_401은_재시도하지_않는다() {
        // given
        String ip = "8.8.8.8";
        fakeIpInfoClient.withException(new HttpClientErrorException(UNAUTHORIZED));

        // when
        IpInfo result = retryableIpInfoClient.getIpInfo(ip);

        // then
        then(result.isUnknown()).isTrue();
        then(fakeIpInfoClient.getCallCount(ip)).isEqualTo(1);
    }
}
