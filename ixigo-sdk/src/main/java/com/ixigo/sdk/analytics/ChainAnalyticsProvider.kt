package com.ixigo.sdk.analytics

class ChainAnalyticsProvider(vararg val providers: AnalyticsProvider?) : AnalyticsProvider {
  override fun logEvent(event: Event) {
    providers.forEach { it?.logEvent(event) }
  }
}
