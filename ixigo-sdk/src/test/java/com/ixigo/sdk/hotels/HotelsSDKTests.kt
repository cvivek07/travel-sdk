package com.ixigo.sdk.hotels

import android.app.Activity
import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.bus.BusConfig
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.common.CustomChromeTabsHelper
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class HotelsSDKTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: ActivityScenario<FragmentActivity>
  private lateinit var activity: Activity
  private lateinit var hotelsSDK: HotelsSDK

  @Mock lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider, config = Config.ProdConfig)
    hotelsSDK = HotelsSDK.init()
  }

  @Test
  fun `test event on launchHome`(){
    hotelsSDK.launchHome(activity)
    verify(mockAnalyticsProvider).logEvent(eq(Event("hotelStartHome")))
  }

  @Test
  fun `test event on launchTrips`(){
    hotelsSDK.launchTrips(activity)
    verify(mockAnalyticsProvider).logEvent(eq(Event("hotelStartTrips")))
  }


  @After
  fun teardown() {
    IxigoSDK.clearInstance()
    HotelsSDK.clearInstance()
  }

  @Test
  fun `test launch Home on confirmtckt`() {
    val expectedUrl = "https://www.ixigo.com/pwa/initialpage?page=HOTEL_HOME"
    testHotelsHome(clientId = "confirmtckt", expectedUrl)
  }

  private fun testHotelsHome(
    clientId: String,
    expectedUrl: String,
    funnelConfig: FunnelConfig? = null
  ) {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn
              AppInfo(clientId = clientId, apiKey = "any", appVersion = 1, appName = "Ixigo Hotels")
      on { analyticsProvider } doReturn mockAnalyticsProvider
      on { webViewConfig } doReturn WebViewConfig()
      on { config } doReturn Config.ProdConfig
    }
    val application: Application = ApplicationProvider.getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)

    hotelsSDK.launchHome(application, funnelConfig)
    verify(mockIxigoSDK)
      .launchWebActivity(
        anyOrNull(),
        anyOrNull(),
        anyOrNull(),
        anyOrNull()
      )
    verify(mockAnalyticsProvider).logEvent(Event("hotelStartHome"))
  }
}
