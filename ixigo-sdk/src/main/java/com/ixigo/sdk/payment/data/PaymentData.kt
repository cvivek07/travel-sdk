package com.ixigo.sdk.payment.data

import androidx.annotation.Keep

@Keep
data class InitializeInput(
    val merchantId: String,
    val clientId: String,
    val customerId: String,
    val provider: String
)

@Keep data class UpiApp(val appName: String, val packageName: String)

@Keep data class GetAvailableUPIAppsResponse(val apps: List<UpiApp>)

@Keep data class GetAvailableUPIAppsInput(val orderId: String, val provider: String)

@Keep
data class ProcessUpiIntentInput(
    val provider: String,
    val orderId: String,
    val appPackage: String,
    val displayNote: String,
    val clientAuthToken: String,
    val endUrls: List<String>,
    val amount: Double
)

@Keep data class ProcessUpiIntentResponse(val orderId: String)

@Keep data class JuspayAvailableUPIAppsResponse(val availableApps: List<UpiApp>)

@Keep
data class FinishPaymentInput(val transactionId: String, val success: Boolean, val nextUrl: String)

@Keep data class FinishPaymentResponse(val handler: PaymentHandler)

@Keep
enum class PaymentHandler {
  BROWSER,
  NATIVE
}
