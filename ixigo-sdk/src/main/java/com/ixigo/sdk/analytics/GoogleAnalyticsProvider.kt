package com.ixigo.sdk.analytics

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker

internal class GoogleAnalyticsProvider(private val tracker: Tracker) : AnalyticsProvider {

  override fun logEvent(event: Event) {
    val action = event.name
    val category = "action"
    val builder = HitBuilders.EventBuilder().setAction(action).setCategory(category)
    event.properties["label"]?.let { builder.setLabel(it) }
    (event.properties["value"]?.toLongOrNull())?.let { builder.setValue(it) }

    event.properties.forEach { (name, value) ->
      val dimension = EventDimension.values().find { it.propertyName == name }
      dimension?.let { builder.setCustomDimension(it.index, value) }
    }
    tracker.send(builder.build())
  }

  enum class EventDimension(val index: Int, val propertyName: String) {
    CLIENT_ID(1, "clientId"),
    SDK_VERSION(2, "sdkVersion"),
    REFERRER(4, "referrer"),
  }
}
