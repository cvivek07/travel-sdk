package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

/** PaymentProvider that will fulfill payments using PaymentSDK */
class PaymentSDKPaymentProvider : PaymentProvider {
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean {
    val transactionId = input.data["paymentId"] ?: return false
    PaymentSDK.instance.processPayment(activity, transactionId = transactionId) {
      val nextUrl =
          when (it) {
            is Err -> {
              callback(Err(Error("Error processing payment")))
              it.value.nextUrl
            }
            is Ok -> {
              callback(Ok(PaymentResponse(it.value.nextUrl)))
              it.value.nextUrl
            }
          }
      IxigoSDK.instance.launchWebActivity(activity, nextUrl)
    }
    return true
  }
}
