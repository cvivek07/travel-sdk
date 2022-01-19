package com.ixigo.sdk.trains

import android.content.Context
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.SdkSingleton

class TrainsSDK(private val config: Config) {

  /** Opens ConfitmTkt PWA home to search for Train trips */
  fun launchHome(context: Context) {
    IxigoSDK.instance.launchWebActivity(context, getBaseUrl())
  }

  private fun getBaseUrl(): String {
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
      assertNotCreated()

      val instance = TrainsSDK(config = config)
      INSTANCE = instance

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
}
