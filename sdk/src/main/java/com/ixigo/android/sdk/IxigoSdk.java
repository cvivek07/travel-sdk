package com.ixigo.android.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

import com.ixigo.android.sdk.util.AuthTokenUtil;

/**
 * Created by bharat on 05/04/18.
 */

public class IxigoSdk {

    private Config config;
    private String authToken;

    private static IxigoSdk ixigoSdk;

    private IxigoSdk(Context context, Config config) {
        this.config = config;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static void init(Application application, Config config) {
        ixigoSdk = new IxigoSdk(application, config);
        String authToken = AuthTokenUtil.getAuthToken(application);
        if (authToken != null) {
            ixigoSdk.authToken = authToken;
        }
    }

    public static IxigoSdk getInstance() {
        if (ixigoSdk == null) {
            throw new RuntimeException(new Exception("ixigo SDK has not been initialized!"));
        }
        return ixigoSdk;
    }

    public static boolean isInitialized() {
        return ixigoSdk != null;
    }

    public static boolean isInitializable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public Config getConfig() {
        return config;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void logIn(Context context, String authToken) {
        AuthTokenUtil.saveAuthToken(context, authToken);
        this.authToken = authToken;
    }

    public void launchFeature(AppCompatActivity appCompatActivity, Feature feature) {
        Intent intent = new Intent(appCompatActivity, IxigoSdkActivity.class);
        intent.putExtra(IxigoSdkActivity.KEY_ACTIVITY_PARAMS, feature);
        appCompatActivity.startActivity(intent);
    }

    public void launchFeatureWithActivityBackStack(AppCompatActivity appCompatActivity, Feature feature, TaskStackBuilder taskStackBuilder) {
        Intent intent = new Intent(appCompatActivity, IxigoSdkActivity.class);
        intent.putExtra(IxigoSdkActivity.KEY_ACTIVITY_PARAMS, feature);
        taskStackBuilder.addNextIntent(intent);
        taskStackBuilder.startActivities();
    }

    public void logOut(Context context) {
        AuthTokenUtil.clearAuthToken(context);
        this.authToken = null;
    }

}
