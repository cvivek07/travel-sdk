package com.ixigo.sdk.analytics

import android.os.Bundle

interface AnalyticsProvider {
  val enabled: Boolean
  fun logEvent(name: String, params: Bundle)
}
