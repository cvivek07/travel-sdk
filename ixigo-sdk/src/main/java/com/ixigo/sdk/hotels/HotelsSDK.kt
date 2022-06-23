package com.ixigo.sdk.hotels

import android.content.Context
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
class HotelsSDK(
    private val customChromeTabsHelper: CustomChromeTabsHelper = CustomChromeTabsHelper()
) {

  fun launchHome(context: Context) {
    val url = "https://www.booking.com"
    customChromeTabsHelper.openUrl(context, url)
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
