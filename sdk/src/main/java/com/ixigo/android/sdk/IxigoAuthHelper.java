package com.ixigo.android.sdk;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bharat on 09/04/18.
 */

public abstract class IxigoAuthHelper extends AndroidViewModel implements AuthTask.Callback {

    public static final String TAG = IxigoAuthHelper.class.getSimpleName();

    private MutableLiveData<String> authTokenLiveData = new MutableLiveData<>();

    public IxigoAuthHelper(@NonNull Application application) {
        super(application);
        authTokenLiveData.setValue(IxigoSdk.getInstance().getAuthToken());
    }

    public LiveData<String> getAuthTokenLiveData() {
        return authTokenLiveData;
    }

    protected void initiateIxigoLogin(final String partnerAuthToken) {
        AuthTask authTask = new AuthTask(partnerAuthToken);
        authTask.setCallback(this);
        authTask.execute();
    }

    protected void abortLogin() {
        authTokenLiveData.setValue(null);
    }

    public void logInRequired(AppCompatActivity activity) {
        //TODO send a login request
        onLogInRequired(activity);
    }

    public void logOut(AppCompatActivity activity) {
        IxigoSdk.getInstance().logOut(getApplication());
        onLogOut(activity);
    }

    @Override
    public void onTokenReceived(String authToken) {
        authTokenLiveData.setValue(authToken);
        if (authToken == null) {
            onLoginFailed();
        } else {
            IxigoSdk.getInstance().logIn(getApplication(), authToken);
            onLoginSuccessful();
        }
    }

    abstract protected void onLogInRequired(AppCompatActivity activity);

    abstract protected void onLoginSuccessful();

    abstract protected void onLoginFailed();

    abstract protected void onLogOut(AppCompatActivity activity);

}
