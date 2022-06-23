package com.ixigo.sdk.analytics

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.ixigo.sdk.test.TestData.FakeAppInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class GoogleAnalyticsProviderTest {
  private lateinit var provider: GoogleAnalyticsProvider
  private lateinit var mockTracker: Tracker

  private val sdkVersion = "sdkVersionValue"

  @Before
  fun setup() {
    mockTracker = mock()
    provider = GoogleAnalyticsProvider(mockTracker, appInfo = FakeAppInfo, sdkVersion = sdkVersion)
  }

  @Test
  fun `test logEvent works correctly`() {
    val event =
        Event(
            name = "actionValue",
            properties =
                mapOf(
                    "referrer" to "https://www.ixigo.com/mypage",
                    "label" to "labelValue",
                    "value" to "123"))
    provider.logEvent(event)
    verify(mockTracker)
        .send(
            HitBuilders.EventBuilder()
                .setCategory("action")
                .setAction("actionValue")
                .setValue(123)
                .setLabel("labelValue")
                .setCustomDimension(1, FakeAppInfo.clientId)
                .setCustomDimension(2, sdkVersion)
                .setCustomDimension(4, "https://www.ixigo.com/mypage")
                .build())
  }
}
