package com.ixigo.sdk.payment.data

import androidx.annotation.Keep

@Keep
data class InitializeInput(
    val merchantId: String,
    val clientId: String,
    val customerId: String,
    val provider: String,
    val environment: String?
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

@Keep data class ProcessGatewayPaymentResponse(val orderId: String)

@Keep data class JuspayAvailableUPIAppsResponse(val availableApps: List<UpiApp>)

@Keep
data class FinishPaymentInput(val transactionId: String, val success: Boolean, val nextUrl: String)

@Keep data class FinishPaymentResponse(val handler: PaymentHandler)

@Keep
data class CredEligibilityInput(
    val provider: String,
    val orderId: String,
    val amount: Double,
    val customerMobile: String,
    val gatewayReferenceId: String
)

@Keep data class CredEligibilityResponse(val eligible: Boolean)

@Keep data class JuspayCredEligibilityResponse(val apps: List<JuspayCredEligibilityResponseApps>)

@Keep
data class ProcessCredPaymentInput(
    val provider: String,
    val orderId: String,
    val amount: Double,
    val clientAuthToken: String,
    val customerMobile: String,
    val gatewayReferenceId: String
)

@Keep
data class JuspayCredEligibilityResponseApps(
    val paymentMethodsEligibility: List<JuspayPaymentMethodsEligibility>
)

@Keep data class JuspayPaymentMethodsEligibility(val isEligible: Boolean)

@Keep data class PhonePeAvailabilityResponse(val available: Boolean)

@Keep data class PhonePeVersionCode(val versionCode: Long)

@Keep data class PhonePeRedirectData(val redirectType: String, val redirectUrl: String)

@Keep data class PhonePePaymentFinished(val paymentFinished: Boolean)

@Keep
data class GpayPaymentInput(
    val amount: String,
    val payeeVpa: String,
    val payeeName: String,
    val referenceUrl: String,
    val mcc: String,
    val transactionReferenceId: String,
    val transactionId: String,
    val transactionNote: String
)

@Keep data class PaymentFinished(val paymentFinished: Boolean)

@Keep data class IxigoSDKVersion(val version: String)

@Keep
data class MinkasuInput(
    val merchantId: String,
    val merchantToken: String,
    val transactionId: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val ctaColor: String
)

@Keep
enum class PaymentHandler {
  BROWSER,
  NATIVE
}
