package com.ixigo.sdk.covid

import android.content.Context
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event

fun IxigoSDK.covidLaunchAppointments(context: Context) {
  val url = getUrl(mapOf("page" to "VACCINE"))
  launchWebActivity(context, url)
  analyticsProvider.logEvent(Event.with(action = "covidAppointmentHome"))
}
