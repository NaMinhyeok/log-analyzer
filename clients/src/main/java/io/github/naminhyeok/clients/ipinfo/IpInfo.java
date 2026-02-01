package io.github.naminhyeok.clients.ipinfo;

public record IpInfo(
    String ip,
    String country,
    String region,
    String city,
    String org
) {

    public static IpInfo unknown(String ip) {
        return new IpInfo(ip, null, null, null, null);
    }

    public boolean isUnknown() {
        return country == null && region == null && city == null && org == null;
    }
}
