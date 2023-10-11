package com.ixigo.sdk.hotels

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.common.CustomChromeTabsHelper
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
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
  @Mock lateinit var mockCustomChromeTokenProvider: CustomChromeTabsHelper

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)

    hotelsSDK = HotelsSDK()
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
    HotelsSDK.clearInstance()
  }

  @Test
  fun `test launch Home`() {
    hotelsSDK.launchHome(activity)
    verify(mockCustomChromeTokenProvider).openUrl(activity, "https://www.booking.com")
  }
}
