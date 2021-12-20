package com.ixigo.sdk.test

import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.analytics.test.FakeAnalyticsProvider

object TestData {
  val FakeAppInfo = AppInfo("clientId", "apiKey", "appVersion", "deviceId", "uuid")

  val DisabledAnalyticsProvider = FakeAnalyticsProvider()
}
