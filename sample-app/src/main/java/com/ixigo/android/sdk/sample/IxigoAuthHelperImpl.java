package com.ixigo.android.sdk.sample;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ixigo.android.sdk.IxigoAuthHelper;

public class IxigoAuthHelperImpl extends IxigoAuthHelper {

    public IxigoAuthHelperImpl(@NonNull Application application) {
        super(application);
    }

    @Override
    public void onLogInRequired(AppCompatActivity activity) {
        Log.i(TAG, "onLogInRequired: ");
        // partner app generates an auth token and invokes initiateIxigoLogin()
        initiateIxigoLogin("<partner-auth-token>");
    }

    @Override
    protected void onLoginSuccessful() {
        Log.i(TAG, "onLoginSuccessful: ");
    }

    @Override
    protected void onLoginFailed() {
        Log.i(TAG, "onLoginFailed: ");
    }

    @Override
    public void onLogOut(AppCompatActivity activity) {
        Log.i(TAG, "onLogOut: ");
    }
}
