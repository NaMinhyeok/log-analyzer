package io.github.naminhyeok.clients.ipinfo;

import org.springframework.web.client.RestClient;

public class IpInfoHttpClient implements IpInfoClient {

    private final RestClient restClient;

    public IpInfoHttpClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public IpInfo getIpInfo(String ip) {
        return restClient.get()
            .uri("/{ip}", ip)
            .retrieve()
            .body(IpInfo.class);
    }
}
