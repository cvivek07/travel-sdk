package com.ixigo.android.sdk;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.ixigo.android.sdk.util.IxigoUrlUtils;
import com.ixigo.android.sdk.util.JsonUtils;
import com.ixigo.android.sdk.util.PackageUtils;
import com.ixigo.android.sdk.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by bharat on 05/04/18.
 */

public class IxigoSdkActivity extends AppCompatActivity {

    private static final String TAG = IxigoSdkActivity.class.getSimpleName();

    public static final String KEY_ACTIVITY_PARAMS = "KEY_ACTIVITY_PARAMS";

    private WebView webView;
    private ProgressBar progressBar;
    private Feature feature;
    private IxigoAuthHelper ixigoAuthHelper;
    private String logInSuccessJsFunction;
    private String logInFailureJsFunction;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ixigo_sdk);
        initViews();

        feature = (Feature) getIntent().getSerializableExtra(KEY_ACTIVITY_PARAMS);
        if (savedInstanceState == null) {
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSaveFormData(false);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(PackageUtils.isDebuggable(this));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            }
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
            webView.setFocusable(true);
            webView.setWebViewClient(getWebViewClient());
            webView.setWebChromeClient(getWebChromeClient());
            webView.getSettings().setGeolocationEnabled(feature.isGeolocationEnabled());

            webView.addJavascriptInterface(this, "IxiWebView");

            Map<String, String> map = new HashMap<>();
            map.put("clientId", IxigoSdk.getInstance().getConfig().getClientId());
            map.put("apiKey", IxigoSdk.getInstance().getConfig().getApiKey());
            map.put("appVersion", String.valueOf(IxigoSdk.getInstance().getConfig().getAppVersion()));
            map.put("deviceId", IxigoSdk.getInstance().getConfig().getDeviceId());
            map.put("uuid", IxigoSdk.getInstance().getConfig().getUuid());
            if (IxigoSdk.getInstance().getAuthToken() != null) {
                map.put("Authorization", IxigoSdk.getInstance().getAuthToken());
            }
            map.put("currencyCode", IxigoSdk.getInstance().getConfig().getCurrencyCode());

            String url = IxigoUrlUtils.buildUrl(feature.getPath());
            webView.loadUrl(url, map);

            Log.i(TAG, "URL: " + url);
        } else {
            webView.restoreState(savedInstanceState);
        }

        Class<? extends IxigoAuthHelper> ixigoAuthCallbacksImplClass = IxigoSdk.getInstance().getConfig().getIxigoAuthHelperImplClass();
        if (ixigoAuthCallbacksImplClass != null) {
            ixigoAuthHelper = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ixigoAuthCallbacksImplClass);
            ixigoAuthHelper.getAuthTokenLiveData().observe(this, tokenObserver);
        }
    }

    private Observer<String> tokenObserver = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String authToken) {
            if (logInSuccessJsFunction != null && StringUtils.isNotEmpty(authToken)) {
                webView.loadUrl(logInSuccessJsFunction.replace("AUTH_TOKEN", authToken));
            } else if (logInFailureJsFunction != null && StringUtils.isEmpty(authToken)) {
                webView.loadUrl(logInFailureJsFunction);
            }
        }
    };

    private void initViews() {
        progressBar = findViewById(R.id.pb_progress);
        webView = findViewById(R.id.webview);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        if (webView != null && isFinishing()) {
            webView.setVisibility(View.GONE);
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    protected class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.startsWith("ixigo:") || url.startsWith("ixigotrains:") || url.startsWith("market:") || url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("whatsapp://")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    protected class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            if (!feature.isProgressBarEnabled()) {
                return;
            }

            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

    }

    protected android.webkit.WebViewClient getWebViewClient() {
        return new WebViewClient();
    }

    protected android.webkit.WebChromeClient getWebChromeClient() {
        return new WebChromeClient() {

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, true);
            }
        };
    }

    @JavascriptInterface
    public void quit() {
        Log.i(TAG, "@JavascriptInterface quit()");
        finish();
    }

    @JavascriptInterface
    public String getAppData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("clientId", IxigoSdk.getInstance().getConfig().getClientId());
            jsonObject.put("deviceId", IxigoSdk.getInstance().getConfig().getDeviceId());
            jsonObject.put("uuid", IxigoSdk.getInstance().getConfig().getUuid());
            jsonObject.put("apiKey", IxigoSdk.getInstance().getConfig().getApiKey());
            jsonObject.put("appVersion", IxigoSdk.getInstance().getConfig().getAppVersion());
            if (IxigoSdk.getInstance().getAuthToken() != null) {
                jsonObject.put("Authorization", IxigoSdk.getInstance().getAuthToken());
            }

            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("apiLevel", Build.VERSION.SDK_INT);
            deviceInfo.put("brand", Build.BRAND);
            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("device", Build.DEVICE);
            deviceInfo.put("product", Build.PRODUCT);

            jsonObject.put("deviceInfo", deviceInfo);
            jsonObject.put("currencyCode", IxigoSdk.getInstance().getConfig().getCurrencyCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @JavascriptInterface
    public void loginUser(final String logInSuccessJsFunction, final String logInFailureJsFunction) {
        this.logInSuccessJsFunction = logInSuccessJsFunction;
        this.logInFailureJsFunction = logInFailureJsFunction;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ixigoAuthHelper == null) {
                    return;
                }
                ixigoAuthHelper.logInRequired(IxigoSdkActivity.this);
            }
        });
    }

    @JavascriptInterface
    public void trackEvent(String eventName, String eventJson) {
        Map<String, Object> propertyNameToValue = buildEventPropertyMap(eventJson);
        EventCallback eventCallback = IxigoSdk.getInstance().getConfig().getEventCallback();
        if (eventCallback != null) {
            eventCallback.onEvent(eventName, propertyNameToValue);
        }

    }

    private Map<String, Object> buildEventPropertyMap(String eventJson) {
        Map<String, Object> propertyMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(eventJson);

            // Parsing for date keys
            if (JsonUtils.isParsable(jsonObject, "dateKeys")) {
                JSONArray dateKeysJsonArray = JsonUtils.getJsonArray(jsonObject, "dateKeys");
                for (int i = 0; i < dateKeysJsonArray.length(); i++) {
                    String key = dateKeysJsonArray.getString(i);
                    String dateString = JsonUtils.getStringVal(jsonObject, key);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH);
                    Date date = simpleDateFormat.parse(dateString);
                    propertyMap.put(key, date);
                }
            }

            Iterator<String> it = jsonObject.keys();
            while (it.hasNext()) {
                String key = it.next();
                // Don't add the key if it has been added in the previous step
                if (propertyMap.containsKey(key)) {
                    continue;
                } else if ("dateKeys".equals(key)) {
                    continue;
                }

                Object object = jsonObject.get(key);
                propertyMap.put(key, object);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return propertyMap;
    }

    @JavascriptInterface
    public void logoutUser() {
        ixigoAuthHelper.logOut(this);
    }

}
