package com.ixigo.android.sdk;

import android.content.Context;
import android.util.Log;

import com.ixigo.android.sdk.util.DeviceUtils;
import com.ixigo.android.sdk.util.StringUtils;
import com.ixigo.android.sdk.util.UuidHelper;

/**
 * Created by bharat on 05/04/18.
 */

public class Config {

    private String currencyCode;
    private String clientId;
    private String apiKey;
    private String deviceId;
    private String uuid;
    private int appVersion;
    private Class<? extends IxigoAuthHelper> ixigoAuthHelperImplClass;
    private EventCallback eventCallback;

    private boolean stagingModeEnabled;

    private Config() {

    }

    private void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    private void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    private void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    private void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    private void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    private void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }

    public int getAppVersion() {
        return appVersion;
    }

    private void setEventCallback(EventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    public EventCallback getEventCallback() {
        return eventCallback;
    }

    private void setIxigoAuthHelperImplClass(Class<? extends IxigoAuthHelper> ixigoAuthHelperImplClass) {
        this.ixigoAuthHelperImplClass = ixigoAuthHelperImplClass;
    }

    public Class<? extends IxigoAuthHelper> getIxigoAuthHelperImplClass() {
        return ixigoAuthHelperImplClass;
    }

    private void setStagingModeEnabled(boolean stagingModeEnabled) {
        this.stagingModeEnabled = stagingModeEnabled;
    }

    public boolean isStagingModeEnabled() {
        return stagingModeEnabled;
    }

    public static class Builder {

        public static final String TAG = Builder.class.getSimpleName();

        private Context context;

        private String currencyCode;
        private String clientId;
        private String apiKey;
        private String deviceId;
        private String uuid;
        private int appVersion;
        private Class<? extends IxigoAuthHelper> ixigoAuthHelperImplClass;
        private EventCallback eventCallback;

        private boolean stagingModeEnabled;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder appVersion(int appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder ixigoAuthHelperImplClass(Class<? extends IxigoAuthHelper> ixigoAuthHelperImplClass) {
            this.ixigoAuthHelperImplClass = ixigoAuthHelperImplClass;
            return this;
        }

        public Builder eventCallback(EventCallback eventCallback) {
            this.eventCallback = eventCallback;
            return this;
        }

        public Builder stagingModeEnabled(boolean enabled) {
            this.stagingModeEnabled = enabled;
            return this;
        }

        public Config build() {
            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(apiKey)) {
                throw new RuntimeException("clientId and/or apiKey cannot be empty");
            }

            if (ixigoAuthHelperImplClass == null) {
                Log.w(TAG, "authCallbacksImplClass not set, SSO functionality will not work.");
            }

            if (eventCallback == null) {
                Log.w(TAG, "eventCallback not set, no analytics callbacks will be received.");
            }

            if (currencyCode == null) {
                Log.i(TAG, "No currency code specified. Using INR.");
                currencyCode = "INR";
            }

            Config config = new Config();
            config.setCurrencyCode(currencyCode);
            config.setApiKey(apiKey);
            config.setClientId(clientId);
            config.setDeviceId(DeviceUtils.getDeviceId(context));
            config.setUuid(UuidHelper.getInstance(context).getUuid().toString());
            config.setAppVersion(appVersion);
            config.setIxigoAuthHelperImplClass(ixigoAuthHelperImplClass);
            config.setEventCallback(eventCallback);
            config.setStagingModeEnabled(stagingModeEnabled);

            return config;
        }
    }
}
