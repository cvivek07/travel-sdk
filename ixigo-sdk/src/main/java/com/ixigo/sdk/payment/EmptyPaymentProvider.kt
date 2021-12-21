package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity

internal object EmptyPaymentProvider : PaymentProvider {
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ) = false
}
