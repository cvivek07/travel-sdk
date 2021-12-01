package com.ixigo.sdk.test

import android.os.Bundle
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.analytics.AnalyticsProvider

object TestData {
    val FakeAppInfo = AppInfo(
        "clientId", "apiKey", "appVersion", "deviceId", "uuid"
    )

    val DisabledAnalyticsProvider = object: AnalyticsProvider {
        override val enabled: Boolean
            get() = false

        override fun logEvent(name: String, params: Bundle) {
        }
    }
}