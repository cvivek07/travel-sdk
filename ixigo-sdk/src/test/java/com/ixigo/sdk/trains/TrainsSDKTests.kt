package com.ixigo.sdk.trains

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.initializeTestIxigoSDK
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

  @Test
  fun `test init sends correct analytics event`() {
    val mockAnalyticsProvider: AnalyticsProvider = mock()
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    TrainsSDK.init()
    verify(mockAnalyticsProvider)
        .logEvent(
            Event(
                name = "sdkInit",
                properties = mapOf("sdk" to "trains", "sdkVersion" to BuildConfig.SDK_VERSION)))
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
    testBusHome(clientId = "abhibus", expectedUrl = "https://trains.abhibus.com/")
  }

  @Test
  fun `test bus home for abhibus and staging`() {
    testBusHome(
        clientId = "abhibus",
        expectedUrl = "https://abhibus-staging.confirmtkt.com/",
        config = TrainsSDK.Config.STAGING)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test trains home for unknown clientId throws Exception`() {
    testBusHome(clientId = "ixigo", expectedUrl = "nothing")
  }

  private fun testBusHome(clientId: String, expectedUrl: String, config: TrainsSDK.Config? = null) {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn AppInfo(clientId = clientId, apiKey = "any", appVersion = 1)
      on { analyticsProvider } doReturn mock()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (config != null) TrainsSDK.init(config = config) else TrainsSDK.init()
    busSDK.launchHome(application)
    verify(mockIxigoSDK).launchWebActivity(application, expectedUrl)
  }
}
