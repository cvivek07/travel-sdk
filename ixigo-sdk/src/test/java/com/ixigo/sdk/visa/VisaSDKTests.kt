package com.ixigo.sdk.covid

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.FunnelConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class VisaSDKTests {

  private lateinit var scenario: ActivityScenario<Activity>
  private lateinit var activity: Activity

  @Mock private lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test visaLaunch`() {
    testVisaLaunch(
        "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=VACCINE",
        Config.ProdConfig)
  }

  @Test
  fun `test visaLaunch with funnel config`() {
    testVisaLaunch(
        "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=VACCINE",
        Config.ProdConfig,
        FunnelConfig(enableExitBar = false))
  }

  private fun testVisaLaunch(
      expectedUrl: String,
      config: Config,
      funnelConfig: FunnelConfig? = null
  ) {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider, config = config)
    IxigoSDK.instance.covidLaunchAppointments(activity, funnelConfig)
    assertLaunchedIntent(activity, expectedUrl)
    verify(mockAnalyticsProvider).logEvent(Event("covidAppointmentHome"))
  }
}
