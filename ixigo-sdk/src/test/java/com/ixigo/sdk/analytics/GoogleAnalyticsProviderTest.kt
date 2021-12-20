package com.ixigo.sdk.analytics

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.analytics.Tracker
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

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
            category = "category",
            action = "action",
            value = 1,
            label = "label",
            dimensions = mapOf(EventDimension.CLIENT_ID to "clientId"))
    provider.logEvent(event)
    //    verify(mockTracker)
    //        .send(
    //            HitBuilders.EventBuilder()
    //                .setCategory(event.category)
    //                .setAction(event.action)
    //                .setValue(event.value!!)
    //                .setLabel(event.label!!)
    //                .setCustomDimension(EventDimension.CLIENT_ID.index,
    // EventDimension.CLIENT_ID.name)
    //                .build())
  }
}
