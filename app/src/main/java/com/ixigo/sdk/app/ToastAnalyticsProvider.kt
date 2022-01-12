package com.ixigo.sdk.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event

class ToastAnalyticsProvider(private val activity: Activity): AnalyticsProvider {
  override fun logEvent(event: Event) {
    activity.runOnUiThread {
      Toast.makeText(activity, event.toString(), Toast.LENGTH_LONG).show()
    }
  }
}