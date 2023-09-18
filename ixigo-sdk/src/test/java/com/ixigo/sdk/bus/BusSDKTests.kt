package com.ixigo.sdk.bus

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.defaultIntentHeaders
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.*
import java.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class BusSDKTests {

  @get:Rule val activityRule = ActivityScenarioRule(Activity::class.java)

  @Before
  fun setup() {
    IxigoSDK.clearInstance()
    BusSDK.clearInstance()
  }

  @Test
  fun `test bus home launch url has app info headers`() {
    testBusHome(clientId = "iximatr", "https://www.abhibus.com/ixigopwa?source=ixtrains")
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init with uninitialized IxigoSDK throws an exception`() {
    BusSDK.init()
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init twice throws an exception`() {
    initializeTestIxigoSDK(
        appInfo =
            AppInfo(clientId = "iximatr", apiKey = "any", appVersion = 1, appName = "Ixigo Trains"))
    BusSDK.init()
    BusSDK.init()
  }

  @Test
  fun `test bus home for ixigo trains`() {
    testBusHome(clientId = "iximatr", "https://www.abhibus.com/ixigopwa?source=ixtrains")
  }

  @Test
  fun `test bus home for confirmTkt`() {
    testBusHome(clientId = "confirmtckt", "https://www.abhibus.com/confirmtkt")
  }

  @Test
  fun `test bus home for ixigo trains with Funnel Config`() {
    testBusHome(
        clientId = "iximatr",
        "https://www.abhibus.com/ixigopwa?source=ixtrains",
        funnelConfig = FunnelConfig(enableExitBar = false))
  }

  @Test
  fun `test bus home for ixigo trains staging`() {
    testBusHome(
        clientId = "iximatr",
        "https://demo.abhibus.com/ixigopwa?source=ixtrains",
        BusConfig.STAGING)
  }

  @Test
  fun `test bus home for ixigo flights`() {
    testBusHome(clientId = "iximaad", "https://www.abhibus.com/ixigopwa?source=ixflights")
  }

  @Test
  fun `test bus home for ixigo flights staging`() {
    testBusHome(
        clientId = "iximaad",
        "https://demo.abhibus.com/ixigopwa?source=ixflights",
        BusConfig.STAGING)
  }

  @Test
  fun `test bus trips for ixigo flights`() {
    testBusTrips(clientId = "iximaad", "https://www.abhibus.com/ixigopwa/trips?source=ixflights")
  }

  @Test
  fun `test bus trips for ixigo trains`() {
    testBusTrips(clientId = "iximatr", "https://www.abhibus.com/ixigopwa/trips?source=ixtrains")
  }

  @Test
  fun `test bus trips for ixigo trains with Funnel Config`() {
    testBusTrips(
        clientId = "iximatr",
        "https://www.abhibus.com/ixigopwa/trips?source=ixtrains",
        funnelConfig = FunnelConfig(enableExitBar = false))
  }

  @Test
  fun `test old bus trips for ixigo trains with Funnel Config`() {
    testOldBusTrips(
        clientId = "iximatr",
        "https://www.ixigo.com/pwa/initialpage?clientId=iximatr&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=BUS_TRIPS",
        funnelConfig = FunnelConfig(enableExitBar = false))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test bus home for other clientId`() {
    testBusHome(clientId = "other", "")
  }

  @Test
  fun `test multi module`() {
    testBusMultiModule(
        "iximatr",
        "https://www.abhibus.com/ixigopwa/search?action=search&jdate=22-01-2022&srcid=3&srcname=Hyderabad&destid=5&destname=Vijayawada&hideHeader=1&source=ixtrains")
  }

  @Test
  fun `test that IxiWebView and HtmlOut are added to Js Interfaces for abhibus url`() {
    initializeTestIxigoSDK(
        appInfo =
            AppInfo(clientId = "iximatr", apiKey = "any", appVersion = 1, appName = "Ixigo Trains"))

    val webViewFragment: WebViewFragment =
        mock<WebViewFragment>().apply { Mockito.`when`(this.viewModel).thenReturn(mock()) }

    val fakeLifecycle = LifecycleRegistry(webViewFragment)
    Mockito.`when`(webViewFragment.lifecycle).thenReturn(fakeLifecycle)

    val busSDK = BusSDK.init(config = BusConfig.PROD)
    val interfaces =
        IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(
            busSDK.config.createUrl("testUrl"), webViewFragment)
    Assert.assertEquals(2, interfaces.size)
    Assert.assertNotNull(interfaces[0] as IxiWebView)
    Assert.assertNotNull(interfaces[1] as HtmlOutJsInterface)
  }

  @Test
  fun `test that IxiWebView is NOT added to Js Interfaces for ixigo url`() {
    initializeTestIxigoSDK(
        appInfo =
            AppInfo(clientId = "iximatr", apiKey = "any", appVersion = 1, appName = "Ixigo Trains"))
    BusSDK.init(config = BusConfig.PROD)
    val webViewFragment: WebViewFragment =
        mock<WebViewFragment>().apply { Mockito.`when`(this.viewModel).thenReturn(mock()) }
    val interfaces =
        IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(
            "https://www.ixigo.com/test", webViewFragment)
    Assert.assertEquals(0, interfaces.size)
  }

  private fun testBusMultiModule(clientId: String, expectedUrl: String, config: BusConfig? = null) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
          AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "Ixigo Trains")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
    }

    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()

    val searchData =
        BusSearchData(
            sourceId = 3,
            sourceName = "Hyderabad",
            destinationId = 5,
            destinationName = "Vijayawada",
            date = LocalDate.parse("2022-01-22"))
    val fragment = busSDK.multiModelFragment(searchData)
    Assert.assertNotNull(fragment as? WebViewFragment)

    val expectedInitialData = InitialPageData(expectedUrl)
    Assert.assertEquals(
        expectedInitialData,
        fragment.arguments!!.getParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS))

    val expectedConfig = FunnelConfig(enableExitBar = false)
    Assert.assertEquals(expectedConfig, fragment.arguments!!.getParcelable(WebViewFragment.CONFIG))

    verify(mockAnalyticsProvider).logEvent(Event("busStartMultiModel"))
  }

  private fun testBusHome(
      clientId: String,
      expectedUrl: String,
      config: BusConfig? = null,
      funnelConfig: FunnelConfig? = null
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
          AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "Ixigo Trains")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()
    busSDK.launchHome(application, funnelConfig)
    verify(mockIxigoSDK)
        .launchWebActivity(
            application,
            expectedUrl,
            funnelConfig,
            mapOf("clientId" to clientId, "appVersion" to "1"))
    verify(mockAnalyticsProvider).logEvent(Event("busStartHome"))
  }

  private fun testBusTrips(
      clientId: String,
      expectedUrl: String,
      config: BusConfig? = null,
      funnelConfig: FunnelConfig? = null
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
          AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "Ixigo Trains")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()
    busSDK.launchTrips(application, funnelConfig)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl, funnelConfig)
    verify(mockAnalyticsProvider).logEvent(Event("busStartTrips"))
  }

  private fun testOldBusTrips(
      clientId: String,
      expectedUrl: String,
      config: BusConfig? = null,
      funnelConfig: FunnelConfig? = null
  ) {
    activityRule.scenario.onActivity { activity ->
      val mockAnalyticsProvider: AnalyticsProvider = mock()
      initializeTestIxigoSDK(
          analyticsProvider = mockAnalyticsProvider,
          appInfo = FakeAppInfo.copy(clientId = clientId))

      val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()
      busSDK.launchAdditionalTrips(activity, funnelConfig)
      assertLaunchedIntent(
          activity,
          expectedUrl,
          defaultIntentHeaders.toMutableMap().apply { put("clientId", clientId) })
      verify(mockAnalyticsProvider).logEvent(Event("busStartAdditionalTrips"))
    }
  }
}
