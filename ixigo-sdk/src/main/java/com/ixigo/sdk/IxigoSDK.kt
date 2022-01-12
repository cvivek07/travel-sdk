package com.ixigo.sdk

import android.content.Context
import android.content.Intent
import com.ixigo.sdk.Config.Companion.ProdConfig
import com.ixigo.sdk.IxigoSDK.Companion.getInstance
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.payment.DisabledPaymentProvider
import com.ixigo.sdk.payment.PaymentProvider
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import java.net.URL

/**
 * This is the main entrypoint to interact with Ixigo SDK.
 *
 * All interactions should happen via its singleton object, [IxigoSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [IxigoSDK.init(...)][init] once when you start-up your
 * Application.
 */
class IxigoSDK
internal constructor(
    internal val appInfo: AppInfo,
    internal val authProvider: AuthProvider,
    internal val paymentProvider: PaymentProvider,
    internal val analyticsProvider: AnalyticsProvider,
    internal val config: Config = ProdConfig
) {

  companion object : SdkSingleton<IxigoSDK>("IxigoSDK") {

    /**
     * Initializes IxigoSDK with required parameters. This method needs to be called before
     * accessing the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     *
     * @param context Android Context. Typically [ApplicationContext]
     * @param authProvider Delegates Authentication logic via this [AuthProvider]
     * @param paymentProvider Delegates Payment logic via this [PaymentProvider]
     * @param appInfo The [AppInfo]
     * @param analyticsProvider [AnalyticsProvider] used throughout the SDK
     */
    @JvmStatic
    fun init(
        context: Context,
        appInfo: AppInfo,
        authProvider: AuthProvider,
        paymentProvider: PaymentProvider = DisabledPaymentProvider,
        analyticsProvider: AnalyticsProvider? = null,
        config: Config = ProdConfig
    ) {
      init(
          context,
          appInfo,
          authProvider,
          paymentProvider,
          commonAnalyticsProvider(context, analyticsProvider),
          config)
    }

    internal fun init(
        context: Context,
        appInfo: AppInfo,
        authProvider: AuthProvider,
        paymentProvider: PaymentProvider,
        analyticsProvider: AnalyticsProvider,
        config: Config = ProdConfig
    ) {
      assertNotCreated()
      INSTANCE =
          IxigoSDK(
              appInfo.replaceDefaults(UUIDFactory(context), DeviceIdFactory(context)),
              authProvider,
              paymentProvider,
              analyticsProvider,
              config)

      analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties =
                  mapOf("clientId" to appInfo.clientId, "sdkVersion" to BuildConfig.SDK_VERSION)))
    }
  }

  /**
   * Opens an Activity to the specified url. It will add all necessary headers based on the provided
   * [AppInfo]
   *
   * @param context
   * @param url url to open the activity
   */
  internal fun launchWebActivity(context: Context, url: String) {
    val intent = Intent(context, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
    context.startActivity(intent)
  }

  internal fun getHeaders(url: String): Map<String, String> {
    if (!isIxigoUrl(url)) {
      return mapOf()
    }
    val headers =
        mutableMapOf(
            "appVersion" to appInfo.appVersionString,
            "clientId" to appInfo.clientId,
            "apiKey" to appInfo.apiKey,
            "deviceId" to appInfo.deviceId,
            "uuid" to appInfo.uuid)
    authProvider.authData?.let { headers["Authorization"] = it.token }
    return headers
  }

  private fun isIxigoUrl(url: String): Boolean {
    return try {
      URL(url).host?.endsWith("ixigo.com") ?: false
    } catch (e: Exception) {
      false
    }
  }
}
