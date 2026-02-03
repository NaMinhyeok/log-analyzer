package io.github.naminhyeok.clients.ipinfo;

public record IpInfo(
    String ip,
    String country,
    String region,
    String city,
    String org
) {

    private static final String UNKNOWN_VALUE = "UNKNOWN";

    public static IpInfo unknown(String ip) {
        return new IpInfo(ip, UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE);
    }

    public boolean isUnknown() {
        return UNKNOWN_VALUE.equals(country)
            && UNKNOWN_VALUE.equals(region)
            && UNKNOWN_VALUE.equals(city)
            && UNKNOWN_VALUE.equals(org);
    }
}
