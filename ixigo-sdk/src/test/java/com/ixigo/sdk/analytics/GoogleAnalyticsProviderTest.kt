package com.ixigo.sdk.analytics

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class GoogleAnalyticsProviderTest {
  private lateinit var provider: GoogleAnalyticsProvider
  private lateinit var mockTracker: Tracker

  @Before
  fun setup() {
    mockTracker = mock()
    provider = GoogleAnalyticsProvider(mockTracker)
  }

  @Test
  fun `test logEvent works correctly`() {
    val event =
        Event(
            name = "actionValue",
            properties =
                mapOf(
                    "clientId" to "clientIdValue",
                    "sdkVersion" to "sdkVersionValue",
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
                .setCustomDimension(
                    GoogleAnalyticsProvider.EventDimension.CLIENT_ID.index, "clientIdValue")
                .setCustomDimension(
                    GoogleAnalyticsProvider.EventDimension.SDK_VERSION.index, "sdkVersionValue")
                .build())
  }
}
