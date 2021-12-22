package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity

/** PaymentProvider used to delegate payments to the Webview */
object DisabledPaymentProvider : PaymentProvider {
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ) = false
}
