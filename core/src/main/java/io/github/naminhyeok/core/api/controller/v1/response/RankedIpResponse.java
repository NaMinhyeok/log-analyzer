package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.RankedItem;

public record RankedIpResponse(
    String value,
    long count,
    double percentage,
    IpDetailResponse ipDetail
) {

    public static RankedIpResponse from(RankedItem rankedItem, IpInfo ipInfo) {
        return new RankedIpResponse(
            rankedItem.value(),
            rankedItem.count(),
            rankedItem.percentage(),
            IpDetailResponse.from(ipInfo)
        );
    }
}
