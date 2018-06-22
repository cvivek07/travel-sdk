package com.ixigo.android.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.ixigo.android.sdk.IxigoSdk;

public class AuthTokenUtil {

    private static final String PREF_FILE_NAME = "com.ixigo.android.sdk.auth_token";
    private static final String PREF_AUTH_TOKEN = "auth_token";
    private static final String PREF_STAGING_AUTH_TOKEN = "staging_auth_token";

    public static void saveAuthToken(Context context, String authToken) {
        Pair<SharedPreferences, String> pair = getPreferencePair(context);
        pair.first.edit().putString(pair.second, authToken).apply();
    }

    public static String getAuthToken(Context context) {
        Pair<SharedPreferences, String> pair = getPreferencePair(context);
        return pair.first.getString(pair.second, null);
    }

    public static void clearAuthToken(Context context) {
        Pair<SharedPreferences, String> pair = getPreferencePair(context);
        pair.first.edit().remove(pair.second).apply();
    }

    private static Pair<SharedPreferences, String> getPreferencePair(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, 0);
        String prefKey = IxigoSdk.getInstance().getConfig().isStagingModeEnabled() ? PREF_STAGING_AUTH_TOKEN : PREF_AUTH_TOKEN;
        return new Pair<>(prefs, prefKey);
    }

}
