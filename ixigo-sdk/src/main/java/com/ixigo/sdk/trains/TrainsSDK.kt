package com.ixigo.sdk.trains

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.webview.*

class TrainsSDK(private val config: Config) : JsInterfaceProvider {

  val analyticsProvider: AnalyticsProvider
    get() = IxigoSDK.instance.analyticsProvider

  /**
   * Opens home to search for Train trips
   *
   * @param context
   */
  fun launchHome(context: Context, config: FunnelConfig? = null) {
    analyticsProvider.logEvent(Event("trainsStartHome"))
    IxigoSDK.instance.launchWebActivity(context, getBaseUrl(), config)
  }

  /**
   * Opens view that displays all train trips the customer has booked
   *
   * @param context
   */
  fun launchTrips(context: Context, config: FunnelConfig? = null) {
    analyticsProvider.logEvent(Event("trainsStartTrips"))
    IxigoSDK.instance.launchWebActivity(context, Config(getBaseUrl()).createUrl("trips"), config)
  }

  /** Fragment that displays all train trips the customer has booked */
  fun tripsFragment(): Fragment {
    analyticsProvider.logEvent(Event("trainsTripsFragment"))
    val arguments =
        Bundle().apply {
          val url: String = Config(getBaseUrl()).createUrl("trips", mapOf("showHeader" to "false"))
          putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url))
          putParcelable(WebViewFragment.CONFIG, FunnelConfig(enableExitBar = false))
        }

    return WebViewFragment().apply { this.arguments = arguments }
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
      jsInterfaces.add(IxiWebView(webViewFragment, viewModel = webViewFragment.viewModel))
      jsInterfaces.add(HtmlOutJsInterface(webViewFragment))
    }
    return jsInterfaces
  }
}
