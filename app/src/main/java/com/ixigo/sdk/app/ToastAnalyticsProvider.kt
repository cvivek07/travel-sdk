package com.ixigo.sdk.app

import android.content.Context
import android.widget.Toast
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event

class ToastAnalyticsProvider(private val context: Context): AnalyticsProvider {
  override fun logEvent(event: Event) {
    Toast.makeText(context, event.toString(), Toast.LENGTH_LONG).show()
  }
}