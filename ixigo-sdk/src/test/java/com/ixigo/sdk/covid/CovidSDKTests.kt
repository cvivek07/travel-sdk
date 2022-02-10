package com.ixigo.sdk.covid

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.IntentMatcher
import com.ixigo.sdk.test.TestData
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.FunnelConfig
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class CovidSDKTests {

  private lateinit var scenario: ActivityScenario<Activity>
  private lateinit var activity: Activity

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test covidLaunchApointment for Prod Config`() {
    testVaccineAppointment(
        "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=VACCINE",
        Config.ProdConfig)
  }

  @Test
  fun `test covidLaunchApointment for Staging Config`() {
    testVaccineAppointment(
        "https://build1.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=VACCINE",
        Config.StagingBuildConfig("build1"))
  }

  @Test
  fun `test covidLaunchApointment with funnel config`() {
    testVaccineAppointment(
        "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=VACCINE",
        Config.ProdConfig,
        FunnelConfig(enableExitBar = false))
  }

  private fun testVaccineAppointment(
      expectedUrl: String,
      config: Config,
      funnelConfig: FunnelConfig? = null
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider, config = config)
    IxigoSDK.instance.covidLaunchAppointments(activity, funnelConfig)
    assertLaunchedIntent(activity, expectedUrl, funnelConfig)
    verify(mockAnalyticsProvider).logEvent(Event("covidAppointmentHome"))
  }

  private fun assertLaunchedIntent(activity: Activity, url: String, config: FunnelConfig?) {
    val intent = Intent(activity, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, expectedHeaders()))
    config?.let { intent.putExtra(WebViewFragment.CONFIG, it) }
    val shadowActivity = Shadows.shadowOf(activity)
    val nextIntent = shadowActivity.nextStartedActivity
    MatcherAssert.assertThat(nextIntent, IntentMatcher(intent))
  }

  private fun expectedHeaders(): Map<String, String> =
      with(TestData.FakeAppInfo) {
        mutableMapOf(
            "appVersion" to appVersionString,
            "clientId" to clientId,
            "apiKey" to apiKey,
            "deviceId" to deviceId,
            "uuid" to uuid,
        )
      }
}
