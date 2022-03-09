@file:JvmName("PaymentSDK")

package com.ixigo.sdk.payment

import android.content.Context
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.webview.FunnelConfig

fun IxigoSDK.processPayment(
    context: Context,
    transactionId: String,
    gatewayId: String = "1",
    config: FunnelConfig? = null
) {
  val url = getUrl(mapOf("page" to "PAYMENT", "gatewayId" to gatewayId, "txnId" to transactionId))
  launchWebActivity(context, url, config)
  analyticsProvider.logEvent(Event.with(action = "paymentsStartHome"))
}
