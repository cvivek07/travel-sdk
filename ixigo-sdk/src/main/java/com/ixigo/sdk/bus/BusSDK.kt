package com.ixigo.sdk.bus

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.ixigo.sdk.*
import com.ixigo.sdk.IxigoSDK.Companion.getInstance
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.bus.BusSDK.Companion.getInstance
import com.ixigo.sdk.common.SdkSingleton

/**
 * This is the main entrypoint to interact with Bus SDK.
 *
 * All interactions should happen via its singleton object, [BusSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [BusSDK.init(...)][init] once when you start-up your
 * Application.
 */
class BusSDK internal constructor(internal val analyticsProvider: AnalyticsProvider) {

  /** Opens Abhibus PWA home to search for Bus trips */
  fun busHome() {
    // TODO
  }

  /**
   * Returns view containing Bus search results for provided search
   *
   * @param searchData
   * @return Fragment with search data content
   */
  fun busMultiModelFragment(searchData: BusSearchData): Fragment {
    // TODO
    return Fragment()
  }

  companion object : SdkSingleton<BusSDK>("BusSDK") {

    /**
     * Initializes BusSDK with required parameters. This method needs to be called before accessing
     * the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     */
    @JvmStatic
    fun init(context: Context, analyticsProvider: AnalyticsProvider? = null) {
      init(context, commonAnalyticsProvider(context, analyticsProvider))
    }

    @VisibleForTesting
    internal fun internalInit(context: Context, analyticsProvider: AnalyticsProvider) {
      assertNotCreated()
      INSTANCE = BusSDK(analyticsProvider)

      analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties = mapOf("sdk" to "bus", "sdkVersion" to BuildConfig.SDK_VERSION)))
    }
  }
}
