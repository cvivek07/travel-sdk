package com.ixigo.android.sdk.util;

import com.ixigo.android.sdk.IxigoSdk;

public class IxigoUrlUtils {

    public static String getBaseUrl() {
        StringBuilder sb = new StringBuilder("https://");
        if (IxigoSdk.getInstance().getConfig().isStagingModeEnabled()) {
            sb.append("build5.ixigo.com");
        } else {
            sb.append("www.ixigo.com");
        }
        return sb.toString();
    }

    public static String buildUrl(String path){
        return getBaseUrl() + path;
    }

}
