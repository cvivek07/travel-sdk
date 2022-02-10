package com.ixigo.sdk.covid

import android.content.Context
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.webview.FunnelConfig

/**
 * Opens Covid Vaccine appointment page
 *
 * @param context
 * @param config Common Funnel configuration
 */
fun IxigoSDK.covidLaunchAppointments(context: Context, config: FunnelConfig? = null) {
  val url = getUrl(mapOf("page" to "VACCINE"))
  launchWebActivity(context, url, config)
  analyticsProvider.logEvent(Event.with(action = "covidAppointmentHome"))
}
