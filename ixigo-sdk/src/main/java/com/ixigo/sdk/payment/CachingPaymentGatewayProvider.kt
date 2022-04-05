package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity

internal class CachingPaymentGatewayProvider(
    private val fragmentActivity: FragmentActivity,
    private val paymentGatewayProvider: PaymentGatewayProvider
) {
  private val providerMap = mutableMapOf<String, PaymentGateway>()

  fun getPaymentGateway(id: String): PaymentGateway? {
    val gateway = providerMap[id]
    if (gateway == null) {
      paymentGatewayProvider.getPaymentGateway(id, fragmentActivity)?.let { providerMap[id] = it }
    }
    return providerMap[id]
  }

  fun clear() {
    providerMap.clear()
  }
}
