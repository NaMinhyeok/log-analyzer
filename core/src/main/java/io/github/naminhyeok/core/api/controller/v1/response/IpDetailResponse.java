package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.clients.ipinfo.IpInfo;

public record IpDetailResponse(
    String country,
    String region,
    String city,
    String org
) {

    public static IpDetailResponse from(IpInfo ipInfo) {
        if (ipInfo == null || ipInfo.isUnknown()) {
            return null;
        }
        return new IpDetailResponse(
            ipInfo.country(),
            ipInfo.region(),
            ipInfo.city(),
            ipInfo.org()
        );
    }
}
