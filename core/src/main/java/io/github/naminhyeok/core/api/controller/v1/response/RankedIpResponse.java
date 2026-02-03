package io.github.naminhyeok.core.api.controller.v1.response;

import io.github.naminhyeok.clients.ipinfo.IpInfo;
import io.github.naminhyeok.core.domain.RankedItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "순위 IP 항목 (상세 정보 포함)")
public record RankedIpResponse(
    @Schema(description = "IP 주소", example = "192.168.1.100")
    String value,
    @Schema(description = "요청 횟수", example = "500")
    long count,
    @Schema(description = "전체 대비 비율 (%)", example = "3.5")
    double percentage,
    @Schema(description = "IP 상세 정보 (조회 실패 시 null)")
    IpDetail ipDetail
) {

    public static RankedIpResponse from(RankedItem rankedItem, IpInfo ipInfo) {
        return new RankedIpResponse(
            rankedItem.value(),
            rankedItem.count(),
            rankedItem.percentage(),
            IpDetail.from(ipInfo)
        );
    }

    @Schema(description = "IP 상세 정보")
    public record IpDetail(
        @Schema(description = "국가 코드", example = "KR")
        String country,
        @Schema(description = "지역", example = "Seoul")
        String region,
        @Schema(description = "도시", example = "Seoul")
        String city,
        @Schema(description = "조직/ISP", example = "Korea Telecom")
        String org
    ) {
        public static IpDetail from(IpInfo ipInfo) {
            if (ipInfo == null || ipInfo.isUnknown()) {
                return null;
            }
            return new IpDetail(
                ipInfo.country(),
                ipInfo.region(),
                ipInfo.city(),
                ipInfo.org()
            );
        }
    }
}
