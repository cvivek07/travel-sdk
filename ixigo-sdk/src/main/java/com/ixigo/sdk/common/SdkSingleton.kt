package com.ixigo.sdk.common

import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.R
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.ChainAnalyticsProvider
import com.ixigo.sdk.analytics.GoogleAnalyticsProvider

abstract class SdkSingleton<T>(private val sdkName: String) {
  protected var INSTANCE: T? = null

  /**
   * Returns SDK singleton.
   *
   * Will throw an Exception if it has not been initialized yet by calling [init]
   *
   * @return SDK singleton
   */
  fun getInstance(): T {
    return INSTANCE
        ?: throw IllegalStateException(
            "${sdkName} has not been initialized. Call `${sdkName}.init()` to initialize it.")
  }

  /** Resets SDK singleton. Used only for tests */
  internal fun clearInstance() {
    INSTANCE = null
  }

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
      clientAnalyticsProvider: AnalyticsProvider?
  ): AnalyticsProvider {
    val tracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.global_tracker)
    val googleAnalyticsProvider = GoogleAnalyticsProvider(tracker)
    return ChainAnalyticsProvider(googleAnalyticsProvider, clientAnalyticsProvider)
  }

  internal fun assertNotCreated() {
    if (INSTANCE != null) {
      throw IllegalStateException("BusSDK has already been initialized")
    }
  }
}
