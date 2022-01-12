package com.ixigo.sdk.test

import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.test.FakeAnalyticsProvider
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider
import com.ixigo.sdk.payment.DisabledPaymentProvider

object TestData {
  val FakeAppInfo = AppInfo("clientId", "apiKey", 1, "deviceId", "uuid")

  val DisabledAnalyticsProvider = FakeAnalyticsProvider()
}

fun initializeTestIxigoSDK() {
  IxigoSDK.replaceInstance(
      IxigoSDK(
          TestData.FakeAppInfo,
          EmptyPartnerTokenProvider,
          DisabledPaymentProvider,
          TestData.DisabledAnalyticsProvider))
}
