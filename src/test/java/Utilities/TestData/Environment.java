package Utilities.TestData;

public enum Environment {
    UAT("https://portal-uat.gimpay.org/Portal/Account/Login"),
    RC("https://portal-uat.gimpay.org/Portal_RoutingRC/Account/Login"),
    PROD("https://portal.gimpay.org/Portal_RoutingRC/Account/Login");

    private final String url;

    Environment(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
