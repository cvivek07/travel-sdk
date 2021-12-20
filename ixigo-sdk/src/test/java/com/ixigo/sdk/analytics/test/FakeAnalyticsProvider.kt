package com.ixigo.sdk.analytics.test

import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event

class FakeAnalyticsProvider : AnalyticsProvider {
  override fun logEvent(event: Event) {}
}
