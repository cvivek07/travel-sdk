package com.ixigo.sdk.trains

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class TrainsSDKTests {

  @Before
  fun setup() {
    IxigoSDK.clearInstance()
    TrainsSDK.clearInstance()
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init with uninitialized IxigoSDK throws an exception`() {
    TrainsSDK.init()
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init twice throws an exception`() {
    initializeTestIxigoSDK()
    TrainsSDK.init()
    TrainsSDK.init()
  }

  @Test
  fun `test trains home for abhibus`() {
    testTrainsHome(clientId = "abhibus", expectedUrl = "https://trains.abhibus.com/")
  }

  @Test
  fun `test trains home for abhibus and staging`() {
    testTrainsHome(
        clientId = "abhibus",
        expectedUrl = "https://abhibus-staging.confirmtkt.com/",
        config = TrainsSDK.Config.STAGING)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test trains home for unknown clientId throws Exception`() {
    testTrainsHome(clientId = "ixigo", expectedUrl = "nothing")
  }

  @Test
  fun `test trains trips for abhibus and prod`() {
    testTrainsTrips(clientId = "abhibus", expectedUrl = "https://trains.abhibus.com/trips")
  }

  @Test
  fun `test trains trips for Abhibus Prod`() {
    initializeTestIxigoSDK(appInfo = FakeAppInfo.copy(clientId = "abhibus"))
    TrainsSDK.init()

    val fragment = TrainsSDK.instance.tripsFragment()
    Assert.assertNotNull(fragment as? WebViewFragment)

    val expectedInitialData = InitialPageData("https://trains.abhibus.com/trips?showHeader=false")
    Assert.assertEquals(
        expectedInitialData,
        fragment.arguments!!.getParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS))

    val expectedConfig = FunnelConfig(enableExitBar = false)
    Assert.assertEquals(expectedConfig, fragment.arguments!!.getParcelable(WebViewFragment.CONFIG))
  }

  private fun testTrainsHome(
      clientId: String,
      expectedUrl: String,
      config: TrainsSDK.Config? = null,
      funnelConfig: FunnelConfig? = null,
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
          AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "appName")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val trainsSDK = if (config != null) TrainsSDK.init(config = config) else TrainsSDK.init()
    trainsSDK.launchHome(application)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl, funnelConfig)
    verify(mockAnalyticsProvider).logEvent(Event("trainsStartHome"))
  }

  private fun testTrainsTrips(
      clientId: String,
      expectedUrl: String,
      config: TrainsSDK.Config? = null,
      funnelConfig: FunnelConfig? = null,
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
          AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "appName")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val trainsSDK = if (config != null) TrainsSDK.init(config = config) else TrainsSDK.init()
    trainsSDK.launchTrips(application)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl, funnelConfig)
    verify(mockAnalyticsProvider).logEvent(Event("trainsStartTrips"))
  }

  @Test
  fun `test that IxiWebView and HtmlOut are added to Js Interfaces for confirmtkt url`() {
    initializeTestIxigoSDK(
        appInfo =
            AppInfo(clientId = "abhibus", apiKey = "abhibus", appVersion = 1, appName = "appName"))
    val trainsSDK = TrainsSDK.init()
    val webViewFragment: WebViewFragment = mock()
    val interfaces =
        IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(
            trainsSDK.getBaseUrl(), webViewFragment)
    Assert.assertEquals(2, interfaces.size)
    Assert.assertNotNull(interfaces[0] as IxiWebView)
    Assert.assertNotNull(interfaces[1] as HtmlOutJsInterface)
  }

  @Test
  fun `test that IxiWebView is NOT added to Js Interfaces for ixigo url`() {
    initializeTestIxigoSDK(
        appInfo =
            AppInfo(clientId = "abhibus", apiKey = "abhibus", appVersion = 1, appName = "appName"))
    TrainsSDK.init()
    val webViewFragment: WebViewFragment = mock()
    val interfaces =
        IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(
            "https://www.ixigo.com/test", webViewFragment)
    Assert.assertEquals(0, interfaces.size)
  }
}
