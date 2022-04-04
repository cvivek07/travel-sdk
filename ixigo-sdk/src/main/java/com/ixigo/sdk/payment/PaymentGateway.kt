package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.payment.data.GetAvailableUPIAppsInput
import com.ixigo.sdk.payment.data.InitializeInput
import com.ixigo.sdk.payment.data.ProcessUpiIntentInput

interface PaymentGateway {
  val initialized: Boolean

  fun initialize(input: InitializeInput, callback: InitializeCallback)

  fun listAvailableUPIApps(input: GetAvailableUPIAppsInput, callback: AvailableUPIAppsCallback)

  fun processUpiIntent(input: ProcessUpiIntentInput, callback: ProcessUpiIntentCallback)
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
