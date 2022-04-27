package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.payment.data.*

interface PaymentGateway {
  val initialized: Boolean

  fun initialize(input: InitializeInput, callback: InitializeCallback)

  fun listAvailableUPIApps(input: GetAvailableUPIAppsInput, callback: AvailableUPIAppsCallback)

  fun processUpiIntent(input: ProcessUpiIntentInput, callback: ProcessGatewayPaymentCallback)

  fun checkCredEligibility(input: CredEligibilityInput, callback: CredEligibilityCallback)

  fun processCredPayment(input: ProcessCredPaymentInput, callback: ProcessGatewayPaymentCallback)
}

interface PaymentGatewayProvider {
  fun getPaymentGateway(id: String, fragmentActivity: FragmentActivity): PaymentGateway?
}

class DefaultPaymentGatewayProvider(private val paymentConfig: PaymentConfig) :
    PaymentGatewayProvider {
  override fun getPaymentGateway(id: String, fragmentActivity: FragmentActivity): PaymentGateway? {
    return when (id) {
      "JUSPAY" -> JusPayGateway(fragmentActivity, paymentConfig.juspayConfig.environment)
      else -> null
    }
  }
}
