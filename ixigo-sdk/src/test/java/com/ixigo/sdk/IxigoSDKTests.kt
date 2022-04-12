package com.ixigo.sdk

import IxigoSDKAndroid
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider
import com.ixigo.sdk.payment.DisabledPaymentProvider
import com.ixigo.sdk.test.IntentMatcher
import com.ixigo.sdk.test.TestData.DisabledAnalyticsProvider
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.defaultTheme
import com.ixigo.sdk.webview.*
import java.lang.IllegalStateException
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class IxigoSDKTests {

  private lateinit var scenario: ActivityScenario<Activity>

  @Before
  fun setup() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test launchWebActivity for logged out user`() {
    val ixigoSDK =
        IxigoSDK(
            FakeAppInfo,
            EmptyPartnerTokenProvider,
            DisabledPaymentProvider,
            DisabledAnalyticsProvider,
            theme = defaultTheme(getApplicationContext()))
    testLaunchActivity("https://www.ixigo.com/page", ixigoSDK)
  }

  @Test
  fun `test init sends correct analytics event`() {
    val analyticsProvider: AnalyticsProvider = mock()
    val context: Context = mock()
    IxigoSDK.clearInstance()
    val ixigoSDK =
        IxigoSDK.init(
            context,
            FakeAppInfo,
            EmptyPartnerTokenProvider,
            DisabledPaymentProvider,
            analyticsProvider)
    assertEquals(FakeAppInfo, ixigoSDK.appInfo)
    assertEquals(DisabledPaymentProvider, ixigoSDK.paymentProvider)
    verify(analyticsProvider)
        .logEvent(
            Event(
                name = "sdkInit",
                properties =
                    mapOf(
                        "clientId" to FakeAppInfo.clientId,
                        "sdkVersion" to BuildConfig.SDK_VERSION)))
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init twice throws an exception`() {
    val analyticsProvider: AnalyticsProvider = mock()
    val context: Context = mock()
    IxigoSDK.init(
        context, FakeAppInfo, EmptyPartnerTokenProvider, DisabledPaymentProvider, analyticsProvider)
    IxigoSDK.init(
        context, FakeAppInfo, EmptyPartnerTokenProvider, DisabledPaymentProvider, analyticsProvider)
  }

  @Test
  fun `test onLogout removes all Cookies`() {
    initializeTestIxigoSDK()
    val cookieManager = CookieManager.getInstance()
    cookieManager.setCookie("https://www.ixigo.com", "cookieName=cookieValue")
    assert(cookieManager.hasCookies())
    IxigoSDK.instance.onLogout()
    assertFalse(cookieManager.hasCookies())
  }

  @Test
  fun `test onLogout removes web data storage`() {
    val mockWebStorage = mock<WebStorage>()
    initializeTestIxigoSDK(webViewConfig = WebViewConfig(webStorage = mockWebStorage))
    IxigoSDK.instance.onLogout()
    verify(mockWebStorage).deleteAllData()
  }

  @Test
  fun `test that IxiWebView is added to Js Interfaces for ixigo url`() {
    testJsInterface(Config.ProdConfig.createUrl("testUrl")) { interfaces ->
      assertTrue(interfaces.any { (it as? IxiWebView) != null })
    }
  }

  @Test
  fun `test that IxiWebView is added for ixigo url regardless of subdomain`() {
    testJsInterface("https://newsubdomain.ixigo.com/test") { interfaces ->
      assertTrue(interfaces.any { (it as? IxiWebView) != null })
    }
  }

  @Test
  fun `test that IxiWebView is NOT added to Js Interfaces for Non ixigo url`() {
    testJsInterface("https://www.confirmtkt.com/test") { interfaces ->
      assertFalse(interfaces.any { (it as? IxiWebView) != null })
    }
  }

  @Test
  fun `test that IxigoAndroidSDK is added to Js Interfaces for any URL`() {
    testJsInterface(Config.ProdConfig.createUrl("testUrl")) { interfaces ->
      assertTrue(interfaces.any { (it as? IxigoSDKAndroid) != null })
    }
  }

  @Test
  fun `test initialized`() {
    assertFalse(IxigoSDK.initialized)
    initializeTestIxigoSDK()
    assertTrue(IxigoSDK.initialized)
  }

  private fun testJsInterface(url: String, check: (List<JsInterface>) -> Unit) {
    val analyticsProvider: AnalyticsProvider = mock()
    IxigoSDK.init(
        getApplicationContext(),
        FakeAppInfo,
        EmptyPartnerTokenProvider,
        DisabledPaymentProvider,
        analyticsProvider)
    val scenario: FragmentScenario<WebViewFragment> =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(
                  WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData("https://www.ixigo.com"))
            })
    scenario.onFragment { webViewFragment ->
      val interfaces = IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(url, webViewFragment)
      check(interfaces)
    }
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
