package com.ixigo.sdk.trains

import android.content.Context
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.webview.*

class TrainsSDK(private val config: Config) : JsInterfaceProvider {

  val analyticsProvider: AnalyticsProvider
    get() = IxigoSDK.instance.analyticsProvider

  /**
   * Opens ConfirmTkt PWA home to search for Train trips
   *
   * @param context
   */
  fun launchHome(context: Context) {
    analyticsProvider.logEvent(Event("trainsStartHome"))
    IxigoSDK.instance.launchWebActivity(context, getBaseUrl())
  }

  internal fun getBaseUrl(): String {
    val clientId = IxigoSDK.instance.appInfo.clientId
    return when (clientId) {
      "abhibus" -> {
        when (config) {
          Config.PROD -> "https://trains.abhibus.com/"
          Config.STAGING -> "https://abhibus-staging.confirmtkt.com/"
        }
      }
      else -> throw IllegalArgumentException("Unsupported clientId=$clientId")
    }
  }

  companion object : SdkSingleton<TrainsSDK>("TrainsSDK") {

    fun init(config: Config = Config.PROD): TrainsSDK {
      assertIxigoSDKIsInitialized()
      assertNotCreated()

      val instance = TrainsSDK(config = config)
      INSTANCE = instance

      IxigoSDK.instance.webViewConfig.addJsInterfaceProvider(instance)

      IxigoSDK.instance.analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties = mapOf("sdk" to "trains", "sdkVersion" to BuildConfig.SDK_VERSION)))
      return instance
    }
  }

  enum class Config {
    STAGING,
    PROD
  }

  override fun getJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface> {
    var jsInterfaces = mutableListOf<JsInterface>()
    if (url.startsWith(getBaseUrl())) {
      jsInterfaces.add(IxiWebView(webViewFragment))
      jsInterfaces.add(HtmlOutJsInterface(webViewFragment))
    }
    return jsInterfaces
  }
}
