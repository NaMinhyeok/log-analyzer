package io.github.naminhyeok.clients.ipinfo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class IpInfoTest {

    @Test
    void IP_정보를_생성할_수_있다() {
        // given
        String ip = "8.8.8.8";
        String country = "US";
        String region = "California";
        String city = "Mountain View";
        String org = "AS15169 Google LLC";

        // when
        IpInfo ipInfo = new IpInfo(ip, country, region, city, org);

        // then
        then(ipInfo)
            .extracting(
                IpInfo::ip,
                IpInfo::country,
                IpInfo::region,
                IpInfo::city,
                IpInfo::org
            )
            .containsExactly(ip, country, region, city, org);
    }

    @Test
    void unknown_IP_정보를_생성할_수_있다() {
        // given
        String ip = "192.168.0.1";

        // when
        IpInfo unknownIpInfo = IpInfo.unknown(ip);

        // then
        then(unknownIpInfo)
            .extracting(
                IpInfo::ip,
                IpInfo::country,
                IpInfo::region,
                IpInfo::city,
                IpInfo::org
            )
            .containsExactly(ip, null, null, null, null);
    }

    @Test
    void unknown_IP_정보는_isUnknown이_true를_반환한다() {
        // given
        IpInfo unknownIpInfo = IpInfo.unknown("192.168.0.1");

        // when
        boolean isUnknown = unknownIpInfo.isUnknown();

        // then
        then(isUnknown).isTrue();
    }

    @Test
    void 정상_IP_정보는_isUnknown이_false를_반환한다() {
        // given
        IpInfo ipInfo = new IpInfo("8.8.8.8", "US", "California", "Mountain View", "Google LLC");

        // when
        boolean isUnknown = ipInfo.isUnknown();

        // then
        then(isUnknown).isFalse();
    }
}
