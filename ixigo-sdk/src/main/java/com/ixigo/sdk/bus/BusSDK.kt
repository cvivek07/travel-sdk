package com.ixigo.sdk.bus

import android.content.Context
import androidx.fragment.app.Fragment
import com.ixigo.sdk.*
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.bus.BusSDK.Companion.init
import com.ixigo.sdk.common.SdkSingleton

/**
 * This is the main entrypoint to interact with Bus SDK.
 *
 * All interactions should happen via its singleton object, [BusSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [BusSDK.init(...)][init] once when you start-up your
 * Application.
 */
class BusSDK(private val config: Config) {

  /** Opens Abhibus PWA home to search for Bus trips */
  fun launchHome(context: Context) {
    IxigoSDK.instance.launchWebActivity(context, pwaBaseUrl)
  }

  /**
   * Returns view containing Bus search results for provided search
   *
   * @param searchData
   * @return Fragment with search data content
   */
  fun multiModelFragment(searchData: BusSearchData): Fragment {
    // TODO
    return Fragment()
  }

  private val pwaBaseUrl: String by lazy {
    val clientId = IxigoSDK.instance.appInfo.clientId

    val path =
        when (clientId) {
          "confirmtckt" -> "confirmtkt"
          "iximatr" -> "ixigo"
          else -> clientId
        }
    config.createUrl(path)
  }

  companion object : SdkSingleton<BusSDK>("BusSDK") {

    /**
     * Initializes BusSDK with required parameters. This method needs to be called before accessing
     * the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     */
    @JvmStatic
    fun init(config: Config = ProdConfig): BusSDK {
      assertIxigoSDKIsInitialized()
      assertNotCreated()

      val instance = BusSDK(config = config)
      INSTANCE = instance

      IxigoSDK.instance.analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties = mapOf("sdk" to "bus", "sdkVersion" to BuildConfig.SDK_VERSION)))
      return instance
    }

    private fun assertIxigoSDKIsInitialized() {
      // This will throw an exception if IxigoSDK is not initialized
      IxigoSDK.instance
    }

    val ProdConfig = Config("https://www.abhibus.com/")
    val StagingConfig = Config("https://demo.abhibus.com/")
  }
}
