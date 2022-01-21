package com.ixigo.sdk.bus

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ixigo.sdk.*
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.bus.BusSDK.Companion.init
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment

/**
 * This is the main entrypoint to interact with Bus SDK.
 *
 * All interactions should happen via its singleton object, [BusSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [BusSDK.init(...)][init] once when you start-up your
 * Application.
 */
class BusSDK(internal val config: Config) {

  val analyticsProvider: AnalyticsProvider
    get() = IxigoSDK.instance.analyticsProvider

  private val cheapestFairCall: CheapestFairCall by lazy { CheapestFairCall(config) }

  /**
   * Get the cheapest fair for a specific trip
   *
   * @param cheapestFareInput
   * @param callback
   */
  fun getCheapestFair(cheapestFareInput: CheapestFareInput, callback: CheapestFareCallback) {
    cheapestFairCall.execute(cheapestFareInput, callback)
  }

  /**
   * Opens Abhibus PWA trips page
   *
   * @param context
   */
  fun launchTrips(context: Context) {
    analyticsProvider.logEvent(Event.with(action = "busStartTrips"))
    IxigoSDK.instance.launchWebActivity(context, config.createUrl("trips", addSkinParam()))
  }

  /**
   * Opens Abhibus PWA home to search for Bus trips
   *
   * @param context
   */
  fun launchHome(context: Context) {
    analyticsProvider.logEvent(Event.with(action = "busStartHome"))
    IxigoSDK.instance.launchWebActivity(context, config.createUrl(null, addSkinParam()))
  }

  private val skin: String?
    get() =
        when (IxigoSDK.instance.appInfo.clientId) {
          "iximaad" -> "ixflights"
          "iximatr" -> "ixtrains"
          else -> null
        }

  private fun addSkinParam(params: Map<String, String> = mapOf()): Map<String, String> {
    val skin = skin
    return if (skin == null) {
      params
    } else {
      params + mapOf("source" to skin)
    }
  }

  /**
   * Returns view containing Bus search results for provided search
   *
   * @param searchData
   * @return Fragment with search data content
   */
  fun multiModelFragment(searchData: BusSearchData): Fragment {
    analyticsProvider.logEvent(Event.with(action = "busStartMultiModel"))
    val arguments =
        Bundle().apply {
          val url =
              config.createUrl(
                  "search",
                  addSkinParam(
                      mapOf(
                          "action" to "search",
                          "jdate" to searchData.dateString,
                          "srcid" to searchData.sourceId.toString(),
                          "srcname" to searchData.sourceName,
                          "destid" to searchData.destinationId.toString(),
                          "destname" to searchData.destinationName,
                          "hideHeader" to "1")))
          putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url))
        }

    return WebViewFragment().apply { this.arguments = arguments }
  }

  companion object : SdkSingleton<BusSDK>("BusSDK") {

    /**
     * Initializes BusSDK with required parameters. This method needs to be called before accessing
     * the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     */
    @JvmStatic
    fun init(config: BusConfig = BusConfig.PROD): BusSDK {
      assertIxigoSDKIsInitialized()
      assertNotCreated()

      val instance = BusSDK(config = Config(getPwaBaseUrl(config)))
      INSTANCE = instance

      IxigoSDK.instance.analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties = mapOf("sdk" to "bus", "sdkVersion" to BuildConfig.SDK_VERSION)))
      return instance
    }

    private fun getPwaBaseUrl(config: BusConfig): String {
      val clientId = IxigoSDK.instance.appInfo.clientId
      return when (clientId) {
        "iximatr", "iximaad" -> {
          when (config) {
            BusConfig.PROD -> "https://www.abhibus.com/ixigopwa"
            BusConfig.STAGING -> "https://demo.abhibus.com/ixigopwa"
          }
        }
        else -> throw IllegalArgumentException("Unsupported clientId=$clientId")
      }
    }

    private fun assertIxigoSDKIsInitialized() {
      // This will throw an exception if IxigoSDK is not initialized
      IxigoSDK.instance
    }
  }
}

enum class BusConfig {
  STAGING,
  PROD
}
