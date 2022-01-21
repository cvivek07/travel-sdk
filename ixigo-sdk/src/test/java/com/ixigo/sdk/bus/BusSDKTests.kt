package com.ixigo.sdk.bus

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import java.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class BusSDKTests {

  @Before
  fun setup() {
    IxigoSDK.clearInstance()
    BusSDK.clearInstance()
  }

  @Test
  fun `test init sends correct analytics event`() {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    initializeTestIxigoSDK(
        analyticsProvider = mockAnalyticsProvider,
        AppInfo(clientId = "iximatr", apiKey = "any", appVersion = 1))
    BusSDK.init()
    verify(mockAnalyticsProvider)
        .logEvent(
            Event(
                name = "sdkInit",
                properties = mapOf("sdk" to "bus", "sdkVersion" to BuildConfig.SDK_VERSION)))
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init with uninitialized IxigoSDK throws an exception`() {
    BusSDK.init()
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init twice throws an exception`() {
    initializeTestIxigoSDK(appInfo = AppInfo(clientId = "iximatr", apiKey = "any", appVersion = 1))
    BusSDK.init()
    BusSDK.init()
  }

  @Test
  fun `test bus home for ixigo trains`() {
    testBusHome(clientId = "iximatr", "https://www.abhibus.com/ixigopwa?source=ixtrains")
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

  private fun testBusMultiModule(clientId: String, expectedUrl: String, config: BusConfig? = null) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn AppInfo(clientId = clientId, apiKey = "any", appVersion = 1)
      on { analyticsProvider } doReturn mockAnalyticsProvider
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
    verify(mockAnalyticsProvider).logEvent(Event("busStartMultiModel"))
  }

  private fun testBusHome(clientId: String, expectedUrl: String, config: BusConfig? = null) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn AppInfo(clientId = clientId, apiKey = "any", appVersion = 1)
      on { analyticsProvider } doReturn mockAnalyticsProvider
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()
    busSDK.launchHome(application)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl)
    verify(mockAnalyticsProvider).logEvent(Event("busStartHome"))
  }

  private fun testBusTrips(clientId: String, expectedUrl: String, config: BusConfig? = null) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn AppInfo(clientId = clientId, apiKey = "any", appVersion = 1)
      on { analyticsProvider } doReturn mockAnalyticsProvider
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) BusSDK.init(config = config) else BusSDK.init()
    busSDK.launchTrips(application)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl)
    verify(mockAnalyticsProvider).logEvent(Event("busStartTrips"))
  }
}
