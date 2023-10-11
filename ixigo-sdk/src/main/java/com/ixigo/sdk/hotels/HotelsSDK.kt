package com.ixigo.sdk.hotels

import android.content.Context
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.*
import com.ixigo.sdk.webview.*

/**
 * This is the main entrypoint to interact with Hotels SDK.
 *
 * All interactions should happen via its singleton object, [HotelsSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [HotelsSDK.init(...)][init] once when you start-up your
 * Application.
 */
class HotelsSDK {

  fun launchHome(context: Context, funnelConfig: FunnelConfig? = null) {
    with(IxigoSDK.instance) {
      analyticsProvider.logEvent(Event.with(action = "hotelStartHome"))
      launchWebActivity(context, getUrl(mapOf("page" to "HOTEL_HOME")), funnelConfig)
    }
  }

  fun launchTrips(context: Context, funnelConfig: FunnelConfig? = null) {
    with(IxigoSDK.instance) {
      analyticsProvider.logEvent(Event.with(action = "hotelStartTrips"))
      launchWebActivity(context, getUrl(mapOf("page" to "HOTEL_BOOKINGS")), funnelConfig)
    }
  }

  companion object : SdkSingleton<HotelsSDK>("HotelsSDK") {

    /**
     * Initializes HotelsSDK with required parameters. This method needs to be called before
     * accessing the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     */
    @JvmStatic
    fun init(): HotelsSDK {
      HotelsSDK.assertNotCreated()

      val instance = HotelsSDK()
      HotelsSDK.INSTANCE = instance

      return instance
    }
  }
}
