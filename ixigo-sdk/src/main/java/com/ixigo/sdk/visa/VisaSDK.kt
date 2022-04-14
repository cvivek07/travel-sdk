package com.ixigo.sdk.visa

import android.content.Context
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.webview.FunnelConfig

/**
 * Opens a flow to acquire a traveling visa
 *
 * @param context
 * @param config Common Funnel configuration
 */
fun IxigoSDK.visaLaunchHome(context: Context, config: FunnelConfig? = null) {
  val url = "https://ixigo.visa2fly.com"
  launchWebActivity(context, url, config)
  analyticsProvider.logEvent(Event.with(action = "visaHome"))
}
