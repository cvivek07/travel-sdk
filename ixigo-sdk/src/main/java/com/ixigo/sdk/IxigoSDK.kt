package com.ixigo.sdk

import android.content.Context
import android.content.Intent
import com.google.android.gms.analytics.GoogleAnalytics
import com.ixigo.sdk.Config.Companion.ProdConfig
import com.ixigo.sdk.IxigoSDK.Companion.getInstance
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.analytics.EventDimension
import com.ixigo.sdk.analytics.GoogleAnalyticsProvider
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.flights.R
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

  companion object {
    private var INSTANCE: IxigoSDK? = null

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
        authProvider: AuthProvider,
        paymentProvider: PaymentProvider,
        appInfo: AppInfo
    ) {
      init(context, authProvider, paymentProvider, appInfo, createGoogleAnalyticsProvider(context))
    }

    internal fun init(
        context: Context,
        authProvider: AuthProvider,
        paymentProvider: PaymentProvider,
        appInfo: AppInfo,
        analyticsProvider: AnalyticsProvider = createGoogleAnalyticsProvider(context)
    ) {
      if (INSTANCE != null) {
        throw IllegalStateException("IxigoSDK has already been initialized")
      }
      INSTANCE = IxigoSDK(appInfo, authProvider, paymentProvider, analyticsProvider)

      analyticsProvider.logEvent(
          Event(
              action = "sdkInit", dimensions = mapOf(EventDimension.CLIENT_ID to appInfo.clientId)))
    }

    /**
     * Returns IxigoSDK singleton.
     *
     * Will throw an Exception if it has not been initialized yet by calling [init]
     *
     * @return IxigoSDK singleton
     */
    @JvmStatic
    fun getInstance(): IxigoSDK {
      return INSTANCE
          ?: throw IllegalStateException(
              "IxigoSDK has not been initialized. Call `IxigoSDK.init()` to initialize it.")
    }

    /** Resets IxigoSDK singleton. Used only for tests */
    internal fun clearInstance() {
      INSTANCE = null
    }

    /**
     * Replaces IxigoSDK singleton. Used only for tests
     *
     * @param newInstance
     */
    internal fun replaceInstance(newInstance: IxigoSDK) {
      INSTANCE = newInstance
    }

    private fun createGoogleAnalyticsProvider(context: Context): GoogleAnalyticsProvider {
      val tracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.global_tracker)
      return GoogleAnalyticsProvider(tracker)
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

  private fun getHeaders(url: String): Map<String, String> {
    if (!isIxigoUrl(url)) {
      return mapOf()
    }
    val headers =
        mutableMapOf(
            "appVersion" to appInfo.appVersion,
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

/**
 * Information about the host App
 *
 * @property clientId
 * @property apiKey
 * @property appVersion
 * @property deviceId
 * @property uuid
 */
data class AppInfo(
    val clientId: String,
    val apiKey: String,
    val appVersion: String,
    val deviceId: String,
    val uuid: String
)
