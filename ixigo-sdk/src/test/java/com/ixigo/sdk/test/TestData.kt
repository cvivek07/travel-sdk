package com.ixigo.sdk.test

import android.graphics.Color
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.DeeplinkHandler
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.test.FakeAnalyticsProvider
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.payment.*
import com.ixigo.sdk.remoteConfig.FakeRemoteConfigProvider
import com.ixigo.sdk.remoteConfig.RemoteConfigProvider
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.ui.Theme
import com.ixigo.sdk.webview.WebViewConfig

object TestData {
  val FakeAppInfo = AppInfo("clientId", "apiKey", 1, "appName", "deviceId", "uuid")

  val DisabledAnalyticsProvider = FakeAnalyticsProvider()
}

internal fun initializeTestIxigoSDK(
    analyticsProvider: AnalyticsProvider = TestData.DisabledAnalyticsProvider,
    appInfo: AppInfo = FakeAppInfo,
    paymentProvider: PaymentProvider = DisabledPaymentProvider,
    partnerTokenProvider: PartnerTokenProvider = EmptyPartnerTokenProvider,
    config: Config = Config.ProdConfig,
    webViewConfig: WebViewConfig = WebViewConfig(),
    deeplinkHandler: DeeplinkHandler? = null,
    theme: Theme = Theme(primaryColor = Color.RED),
    remoteConfig: RemoteConfigProvider = FakeRemoteConfigProvider(),
    authProvider: FakeAuthProvider = FakeAuthProvider("token")
) {
  IxigoSDK.replaceInstance(
      IxigoSDK(
          appInfo,
          partnerTokenProvider,
          paymentProvider,
          analyticsProvider,
          config = config,
          webViewConfig = webViewConfig,
          deeplinkHandler = deeplinkHandler,
          theme = theme,
          remoteConfigProvider = FakeRemoteConfigProvider(),
          authProvider = authProvider
      )
  )
}

internal fun initializePaymentSDK(ssoAuthProvider: SSOAuthProvider? = null) {
  val config =
      PaymentConfig(juspayConfig = JuspayConfig(environment = JusPayEnvironment.PRODUCTION))
  val paymentSDK =
      if (ssoAuthProvider == null) {
        PaymentSDK(config)
      } else {
        PaymentSDK(config, ssoAuthProvider = ssoAuthProvider)
      }
  PaymentSDK.replaceInstance(paymentSDK)
}
