package io.github.naminhyeok.clients.ipinfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class IpInfoHttpClient implements IpInfoClient {

    private final RestClient restClient;

    public IpInfoHttpClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public IpInfo getIpInfo(String ip) {
        try {
            return restClient.get()
                .uri("/{ip}", ip)
                .retrieve()
                .body(IpInfo.class);
        } catch (Exception e) {
            log.warn("Failed to fetch IP info for {}: {}", ip, e.getMessage());
            return IpInfo.unknown(ip);
        }
    }
}
