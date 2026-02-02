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
    private RuntimeException exceptionToThrow = null;
    private int failCount = 0;

    @Override
    public IpInfo getIpInfo(String ip) {
        calledIps.add(ip);

        // 설정된 횟수만큼 실패 후 성공
        if (failCount > 0) {
            int currentCallCount = getCallCount(ip);
            if (currentCallCount <= failCount) {
                throw exceptionToThrow != null ? exceptionToThrow
                    : new RuntimeException("Simulated failure");
            }
        }

        // 항상 예외 발생
        if (exceptionToThrow != null && failCount == 0) {
            throw exceptionToThrow;
        }

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

    public FakeIpInfoClient withException(RuntimeException exception) {
        this.exceptionToThrow = exception;
        return this;
    }

    public FakeIpInfoClient failFirstNAttempts(int n, RuntimeException exception) {
        this.failCount = n;
        this.exceptionToThrow = exception;
        return this;
    }

    public void reset() {
        calledIps.clear();
        exceptionToThrow = null;
        failCount = 0;
        ipInfoMap.clear();
    }
}
