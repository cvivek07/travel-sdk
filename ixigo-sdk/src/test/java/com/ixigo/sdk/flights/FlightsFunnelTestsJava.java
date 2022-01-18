package com.ixigo.sdk.flights;

import android.app.Activity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ixigo.sdk.IxigoSDK;
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider;
import com.ixigo.sdk.test.TestData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FlightsFunnelTestsJava {

    private ActivityScenario<Activity> scenario;
    private Activity activity;

    @Before
    public void setup() {
        scenario = ActivityScenario.launch(Activity.class);
        scenario.onActivity(activity -> FlightsFunnelTestsJava.this.activity = activity);
    }

    @Test
    public void testInitializationWithJava() {
        IxigoSDK.init(activity, TestData.INSTANCE.getFakeAppInfo(), EmptyPartnerTokenProvider.INSTANCE);
        FlightsFunnel.flightsStartHome(IxigoSDK.Companion.getInstance(), activity);
    }
}
