package com.ixigo.sdk.common

import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.R
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.ChainAnalyticsProvider
import com.ixigo.sdk.analytics.GoogleAnalyticsProvider
import com.ixigo.sdk.util.PublishEventProvider

abstract class SdkSingleton<T>(private val sdkName: String) {
  protected var INSTANCE: T? = null

  /**
   * Returns SDK singleton.
   *
   * Will throw an Exception if it has not been initialized yet by calling [init]
   *
   * @return SDK singleton
   */
  val instance: T
    get() =
        INSTANCE
            ?: throw IllegalStateException(
                "${sdkName} has not been initialized. Call `${sdkName}.init()` to initialize it.")

  /** Resets SDK singleton. Used only for tests */
  internal fun clearInstance() {
    INSTANCE = null
  }

  /** Whether or not SDK has been initialized */
  val initialized: Boolean
    get() = INSTANCE != null

  /**
   * Replaces SDK singleton. Used only for tests
   *
   * @param newInstance
   */
  internal fun replaceInstance(newInstance: T) {
    INSTANCE = newInstance
  }

  internal fun commonAnalyticsProvider(
      context: Context,
      appInfo: AppInfo,
      clientAnalyticsProvider: AnalyticsProvider?
  ): AnalyticsProvider {
    val tracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.ixigosdk_tracker)
    val googleAnalyticsProvider = GoogleAnalyticsProvider(tracker, appInfo = appInfo)
    val publishEventProvider = PublishEventProvider()
    return ChainAnalyticsProvider(
        googleAnalyticsProvider, clientAnalyticsProvider, publishEventProvider)
  }

  internal fun assertNotCreated() {
    if (INSTANCE != null) {
      throw IllegalStateException("$sdkName has already been initialized")
    }
  }

  internal fun assertIxigoSDKIsInitialized() {
    // This will throw an exception if IxigoSDK is not initialized
    IxigoSDK.instance
  }
}
