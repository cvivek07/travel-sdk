package com.ixigo.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider
import com.ixigo.sdk.payment.DisabledPaymentProvider
import com.ixigo.sdk.test.IntentMatcher
import com.ixigo.sdk.test.TestData.DisabledAnalyticsProvider
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class IxigoSDKTests {

  private lateinit var scenario: ActivityScenario<Activity>

  @Test
  fun `test launchWebActivity for logged out user`() {
    val ixigoSDK =
        IxigoSDK(
            FakeAppInfo,
            EmptyPartnerTokenProvider,
            DisabledPaymentProvider,
            DisabledAnalyticsProvider)
    testLaunchActivity("https://www.ixigo.com/page", ixigoSDK)
  }

  @Test
  fun `test init sends correct analytics event`() {
    val analyticsProvider: AnalyticsProvider = mock()
    val context: Context = mock()
    IxigoSDK.clearInstance()
    IxigoSDK.init(
        context, FakeAppInfo, EmptyPartnerTokenProvider, DisabledPaymentProvider, analyticsProvider)
    verify(analyticsProvider)
        .logEvent(
            Event(
                name = "sdkInit",
                properties =
                    mapOf(
                        "clientId" to FakeAppInfo.clientId,
                        "sdkVersion" to BuildConfig.SDK_VERSION)))
  }

  private fun testLaunchActivity(
      url: String,
      ixigoSDK: IxigoSDK,
      expectedHeaders: Map<String, String> = expectedHeaders(ixigoSDK)
  ) {
    scenario = launchActivity()
    scenario.onActivity { activity ->
      ixigoSDK.launchWebActivity(activity, url)
      assertLaunchedIntent(activity, url, expectedHeaders)
    }
  }

  private fun assertLaunchedIntent(
      activity: Activity,
      url: String,
      expectedHeaders: Map<String, String>
  ) {
    val intent = Intent(activity, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, expectedHeaders))
    val shadowActivity = Shadows.shadowOf(activity)
    val nextIntent = shadowActivity.nextStartedActivity
    MatcherAssert.assertThat(nextIntent, IntentMatcher(intent))
  }

  private fun expectedHeaders(ixigoSDK: IxigoSDK): Map<String, String> {
    val appInfo = ixigoSDK.appInfo
    return mutableMapOf(
        "appVersion" to appInfo.appVersionString,
        "clientId" to appInfo.clientId,
        "apiKey" to appInfo.apiKey,
        "deviceId" to appInfo.deviceId,
        "uuid" to appInfo.uuid,
    )
  }
}
