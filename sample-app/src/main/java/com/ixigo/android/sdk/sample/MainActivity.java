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
                    .clientId("ecomaxgo")
                    .apiKey("ecomaxgo!2$")
                    .stagingModeEnabled(true)
                    .ixigoAuthHelperImplClass(IxigoAuthHelperImpl.class)
                    .eventCallback(new EventCallbackImpl())
                    .build();

            IxigoSdk.init(this.getApplication(), config);
        }

        findViewById(R.id.btn_flight_search).setOnClickListener(view -> launchFeature(Feature.FLIGHT_HOME));
        findViewById(R.id.btn_flight_trips).setOnClickListener(view -> launchFeature(Feature.FLIGHT_TRIPS));
        findViewById(R.id.btn_hotel_search).setOnClickListener(view -> launchFeature(Feature.HOTEL_HOME));
        findViewById(R.id.btn_hotel_trips).setOnClickListener(view -> launchFeature(Feature.HOTEL_TRIPS));
        findViewById(R.id.btn_bus_search).setOnClickListener(view -> launchFeature(Feature.BUS_HOME));
        findViewById(R.id.btn_bus_trips).setOnClickListener(view -> launchFeature(Feature.BUS_TRIPS));
        findViewById(R.id.btn_wallet).setOnClickListener(view -> launchFeature(Feature.WALLET));

    }

    private void launchFeature(Feature feature) {
        IxigoSdk.getInstance().launchFeature(MainActivity.this, feature);

    }

    public static class EventCallbackImpl implements EventCallback {

        @Override
        public void onEvent(String eventName, Map<String, Object> eventPropertyMap) {

        }
    }

}
