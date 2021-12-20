package com.ixigo.sdk.analytics

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker

class GoogleAnalyticsProvider(private val tracker: Tracker) : AnalyticsProvider {

  //    private val tracker by lazy {
  //        val ga = GoogleAnalytics.getInstance(context)
  //        ga.newTracker(R.xml.global_tracker)
  //    }

  override fun logEvent(event: Event) {
    val builder = HitBuilders.EventBuilder().setAction(event.action).setCategory(event.category)
    event.value?.let { builder.setValue(it) }
    event.label?.let { builder.setLabel(it) }
    for ((dimension, value) in event.dimensions) {
      builder.setCustomDimension(dimension.index, value)
    }
    tracker.send(builder.build())
  }
}
