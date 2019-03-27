package com.ixigo.android.sdk.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ixigo.android.sdk.Config;
import com.ixigo.android.sdk.EventCallback;
import com.ixigo.android.sdk.Feature;
import com.ixigo.android.sdk.IxigoSdk;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (IxigoSdk.isInitializable()) {
            Config config = new Config.Builder(this)
                    .clientId("<client-id>")
                    .apiKey("<api-key>")
                    .stagingModeEnabled(true)
                    .ixigoAuthHelperImplClass(IxigoAuthHelperImpl.class)
                    .eventCallback(new EventCallbackImpl())
                    .build();

            IxigoSdk.init(this.getApplication(), config);
        }

        findViewById(R.id.button).setOnClickListener(view -> {

            if (!IxigoSdk.isInitialized()) {
                return;
            }

            IxigoSdk.getInstance().launchFeature(MainActivity.this, Feature.FLIGHT_SEARCH);
        });

        findViewById(R.id.button2).setOnClickListener(view -> {

            if (!IxigoSdk.isInitialized()) {
                return;
            }

            IxigoSdk.getInstance().launchFeature(MainActivity.this, Feature.TRIPS);
        });

        findViewById(R.id.button3).setOnClickListener(view -> {

            if (!IxigoSdk.isInitialized()) {
                return;
            }

            IxigoSdk.getInstance().launchFeature(MainActivity.this, Feature.WALLET);
        });
    }

    public static class EventCallbackImpl implements EventCallback {

        @Override
        public void onEvent(String eventName, Map<String, Object> eventPropertyMap) {

        }
    }

}
