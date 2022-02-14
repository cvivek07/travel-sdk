package com.ixigo.sdk.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event

class ToastAnalyticsProvider(private val activity: Activity): AnalyticsProvider {
  var enabled = true
  override fun logEvent(event: Event) {
    if (!enabled) {
      return
    }
    activity.runOnUiThread {
      Toast.makeText(activity, event.toString(), Toast.LENGTH_SHORT).show()
    }
  }
}