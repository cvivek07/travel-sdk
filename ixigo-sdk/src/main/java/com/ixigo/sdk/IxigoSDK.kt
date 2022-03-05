package com.ixigo.sdk

import IxigoSDKAndroid
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.annotation.VisibleForTesting
import androidx.test.espresso.idling.net.UriIdlingResource
import com.ixigo.sdk.Config.Companion.ProdConfig
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.payment.DisabledPaymentProvider
import com.ixigo.sdk.payment.PaymentProvider
import com.ixigo.sdk.webview.*
import java.net.URL

/**
 * This is the main entrypoint to interact with Ixigo SDK.
 *
 * All interactions should happen via its singleton object, [IxigoSDK.instance][getInstance].
 *
 * Before using it, you need to call [IxigoSDK.init(...)][init] once when you start-up your
 * Application.
 */
class IxigoSDK
internal constructor(
    internal val appInfo: AppInfo,
    internal val partnerTokenProvider: PartnerTokenProvider,
    internal val paymentProvider: PaymentProvider,
    internal val analyticsProvider: AnalyticsProvider,
    internal val config: Config = ProdConfig,
    internal val webViewConfig: WebViewConfig = WebViewConfig()
) : JsInterfaceProvider {

  @VisibleForTesting
  val uriIdlingResource: UriIdlingResource by lazy {
    UriIdlingResource("IxigoSDKUriIdlingResource", 1000)
  }

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
    @JvmOverloads
    fun init(
        context: Context,
        appInfo: AppInfo,
        partnerTokenProvider: PartnerTokenProvider,
        paymentProvider: PaymentProvider = DisabledPaymentProvider,
        analyticsProvider: AnalyticsProvider? = null,
        config: Config = ProdConfig
    ): IxigoSDK {
      return init(
          context,
          appInfo,
          partnerTokenProvider,
          paymentProvider,
          commonAnalyticsProvider(context, analyticsProvider),
          config)
    }

    internal fun init(
        context: Context,
        appInfo: AppInfo,
        partnerTokenProvider: PartnerTokenProvider,
        paymentProvider: PaymentProvider,
        analyticsProvider: AnalyticsProvider,
        config: Config = ProdConfig
    ): IxigoSDK {
      assertNotCreated()
      val ixigoSDK =
          IxigoSDK(
              appInfo.replaceDefaults(UUIDFactory(context), DeviceIdFactory(context)),
              partnerTokenProvider,
              paymentProvider,
              analyticsProvider,
              config)
      INSTANCE = ixigoSDK

      ixigoSDK.webViewConfig.addJsInterfaceProvider(ixigoSDK)

      analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties =
                  mapOf("clientId" to appInfo.clientId, "sdkVersion" to BuildConfig.SDK_VERSION)))

      return ixigoSDK
    }
  }

  /**
   * Opens an Activity to the specified url. It will add all necessary headers based on the provided
   * [AppInfo]
   *
   * @param context
   * @param url url to open the activity
   */
  internal fun launchWebActivity(context: Context, url: String, config: FunnelConfig? = null) {
    val intent = Intent(context, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
    config?.let { intent.putExtra(WebViewFragment.CONFIG, it) }
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
    // TODO: Figure out a way of getting an Ixigo token at this point if available
    //    authProvider.authData?.let { headers["Authorization"] = it.token }
    return headers
  }

  /**
   * Call this method when the user logs out of the Host App This method will remove every
   * cached/stored user info in the SDK
   */
  fun onLogout() {
    CookieManager.getInstance().removeAllCookies(null)
    webViewConfig.webStorage.deleteAllData()
  }

  internal fun getUrl(properties: Map<String, String>): String {
    val builder =
        Uri.parse(config.apiBaseUrl)
            .buildUpon()
            .appendPath("pwa")
            .appendPath("initialpage")
            .appendQueryParameter("clientId", appInfo.clientId)
            .appendQueryParameter("apiKey", appInfo.apiKey)
            .appendQueryParameter("appVersion", appInfo.appVersionString)
            .appendQueryParameter("deviceId", appInfo.deviceId)
            .appendQueryParameter("languageCode", "en") // TODO
    for (property in properties) {
      builder.appendQueryParameter(property.key, property.value)
    }
    return builder.build().toString()
  }

  private fun isIxigoUrl(url: String): Boolean {
    return try {
      URL(url).host?.endsWith("ixigo.com") ?: false
    } catch (e: Exception) {
      false
    }
  }

  override fun getJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface> {
    var jsInterfaces = mutableListOf<JsInterface>()
    if (url.startsWith(config.apiBaseUrl)) {
      jsInterfaces.add(IxiWebView(webViewFragment))
    }
    jsInterfaces.add(IxigoSDKAndroid(analyticsProvider, webViewFragment))
    return jsInterfaces
  }
}
