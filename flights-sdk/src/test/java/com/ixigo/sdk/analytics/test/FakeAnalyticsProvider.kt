package com.ixigo.sdk.analytics.test

import android.os.Bundle
import com.ixigo.sdk.analytics.AnalyticsProvider

class FakeAnalyticsProvider(override val enabled: Boolean = false) : AnalyticsProvider {
    override fun logEvent(name: String, params: Bundle) {}
}