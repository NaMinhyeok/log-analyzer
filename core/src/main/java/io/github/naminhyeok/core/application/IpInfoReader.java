package io.github.naminhyeok.core.application;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.clients.ipinfo.IpInfoClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class IpInfoReader {

    private final IpInfoClient ipInfoClient;

    public IpInfoReader(IpInfoClient ipInfoClient) {
        this.ipInfoClient = ipInfoClient;
    }

    public IpInfo read(String ip) {
        return ipInfoClient.getIpInfo(ip);
    }

    public Map<String, IpInfo> readAll(List<String> ips) {
        return ips.stream()
            .distinct()
            .collect(Collectors.toMap(
                Function.identity(),
                ipInfoClient::getIpInfo
            ));
    }
}
