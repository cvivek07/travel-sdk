package com.ixigo.android.sdk.util;

public class UrlBuilder {

    public static String getSsoUrl() {
        return IxigoUrlUtils.getBaseUrl() + "/api/v2/oauth/sso/login/token";
    }
}
