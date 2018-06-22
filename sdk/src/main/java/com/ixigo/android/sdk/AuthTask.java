package com.ixigo.android.sdk;

import android.os.AsyncTask;
import android.util.Log;

import com.ixigo.android.sdk.util.JsonUtils;
import com.ixigo.android.sdk.util.UrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = AuthTask.class.getSimpleName();

    private String partnerAuthToken;

    private Callback callback;

    public AuthTask(String partnerAuthToken) {
        this.partnerAuthToken = partnerAuthToken;
    }

    @Override
    protected String doInBackground(Void... voids) {

        FormBody formBody = new FormBody.Builder()
                .add("authCode", partnerAuthToken)
                .build();

        Config config = IxigoSdk.getInstance().getConfig();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(UrlBuilder.getSsoUrl())
                .addHeader("ixiSrc", config.getClientId())
                .addHeader("clientId", config.getClientId())
                .addHeader("apiKey", config.getApiKey())
                .addHeader("deviceId", config.getDeviceId())
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (responseBody == null) {
                return null;
            }

            JSONObject responseJson = new JSONObject(response.body().string());
            Log.i(TAG, "Auth Response: " + responseJson.toString());
            if (JsonUtils.isParsable(responseJson, "data")) {
                JSONObject dataJson = JsonUtils.getJsonObject(responseJson, "data");
                String accessToken = JsonUtils.getStringVal(dataJson, "access_token");
                return accessToken;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String authToken) {
        super.onPostExecute(authToken);

        if (callback != null) {
            callback.onTokenReceived(authToken);
        }

    }

    public interface Callback {
        void onTokenReceived(String authToken);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
