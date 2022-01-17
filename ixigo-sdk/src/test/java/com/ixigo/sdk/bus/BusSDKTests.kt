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
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
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
    initializeTestIxigoSDK()
    BusSDK.init()
    BusSDK.init()
  }

  @Test
  fun `test bus home for confitmTkt`() {
    testBusHome(clientId = "confirmtckt", expectedPath = "confirmtkt")
  }

  @Test
  fun `test bus home for confitmTkt and staging`() {
    testBusHome(clientId = "confirmtckt", expectedPath = "confirmtkt", useStaging = true)
  }

  @Test
  fun `test bus home for ixigo trains`() {
    testBusHome(clientId = "iximatr", expectedPath = "ixigo")
  }

  @Test
  fun `test bus home for other clientId`() {
    testBusHome(clientId = "other", expectedPath = "other")
  }

  private fun testBusHome(clientId: String, expectedPath: String, useStaging: Boolean = false) {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn AppInfo(clientId = clientId, apiKey = "any", appVersion = 1)
      on { analyticsProvider } doReturn mock()
    }
    val application: Application = getApplicationContext()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    val busSDK = if (useStaging) BusSDK.init(config = BusSDK.StagingConfig) else BusSDK.init()
    busSDK.launchHome(application)
    val subdomain = if (useStaging) "demo" else "www"
    verify(mockIxigoSDK)
        .launchWebActivity(application, "https://${subdomain}.abhibus.com/$expectedPath")
  }

  @Test
  fun `test bus multimodel fragment`() {
    // TODO: Redo after implementation of `BusSDK.busHome` is done
  }
}
