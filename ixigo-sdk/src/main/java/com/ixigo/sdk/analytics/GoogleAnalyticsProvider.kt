package com.ixigo.sdk.analytics

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.BuildConfig
import timber.log.Timber

internal class GoogleAnalyticsProvider(
    private val tracker: Tracker,
    private val appInfo: AppInfo,
    private val sdkVersion: String = BuildConfig.SDK_VERSION
) : AnalyticsProvider {

  override fun logEvent(event: Event) {
    Timber.d("logEvent=${event}")
    val action = event.name
    val category = "action"
    val builder = HitBuilders.EventBuilder().setAction(action).setCategory(category)
    event.properties["label"]?.let { builder.setLabel(it) }
    (event.properties["value"]?.toLongOrNull())?.let { builder.setValue(it) }

    event.properties.forEach { (name, value) ->
      val dimension = EventDimension.values().find { it.propertyName == name }
      dimension?.let { builder.setCustomDimension(it.index, value) }
    }
    builder.setCustomDimension(EventDimension.SDK_VERSION.index, sdkVersion)
    builder.setCustomDimension(EventDimension.CLIENT_ID.index, appInfo.clientId)
    tracker.send(builder.build())
  }

  enum class EventDimension(val index: Int, val propertyName: String) {
    CLIENT_ID(1, "clientId"),
    SDK_VERSION(2, "sdkVersion"),
    REFERRER(4, "referrer"),
  }
}
