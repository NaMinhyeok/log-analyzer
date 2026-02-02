package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.clients.ipinfo.IpInfoClient;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class RetryableIpInfoClient implements IpInfoClient {

    private final IpInfoClient delegate;
    private final Retry retry;

    public RetryableIpInfoClient(
        @Qualifier("ipInfoHttpClient") IpInfoClient delegate,
        Retry ipInfoRetry
    ) {
        this.delegate = delegate;
        this.retry = ipInfoRetry;
    }

    @Override
    public IpInfo getIpInfo(String ip) {
        try {
            return Retry.decorateSupplier(retry, () -> delegate.getIpInfo(ip)).get();
        } catch (Exception e) {
            log.warn("All retries failed for IP: {}. Error: {}", ip, e.getMessage());
            return IpInfo.unknown(ip);
        }
    }
}
