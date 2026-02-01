package io.github.naminhyeok.core.support.fake;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.clients.ipinfo.IpInfoClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeIpInfoClient implements IpInfoClient {

    private final Map<String, IpInfo> ipInfoMap = new HashMap<>();
    private final List<String> calledIps = new ArrayList<>();

    @Override
    public IpInfo getIpInfo(String ip) {
        calledIps.add(ip);
        return ipInfoMap.getOrDefault(ip, IpInfo.unknown(ip));
    }

    public FakeIpInfoClient withIpInfo(String ip, IpInfo ipInfo) {
        ipInfoMap.put(ip, ipInfo);
        return this;
    }

    public FakeIpInfoClient withIpInfo(String ip, String country, String region, String city, String org) {
        return withIpInfo(ip, new IpInfo(ip, country, region, city, org));
    }

    public int getCallCount(String ip) {
        return (int) calledIps.stream().filter(ip::equals).count();
    }

    public int getTotalCallCount() {
        return calledIps.size();
    }

    public List<String> getCalledIps() {
        return List.copyOf(calledIps);
    }
}
