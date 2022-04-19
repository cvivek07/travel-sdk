package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.webview.UrlLoader

/** PaymentProvider that will fulfill payments using PaymentSDK */
internal class PaymentSDKPaymentProvider : PaymentProvider {
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean {
    val flowType = input.data["flowType"] ?: "PAYMENT_SDK"

    val transactionId =
        when (flowType) {
          "PAYMENT_SDK" -> input.data["paymentTransactionId"]
          else -> input.data["paymentId"]
        }
            ?: return false

    PaymentSDK.instance.processPayment(
        activity,
        transactionId = transactionId,
        flowType = flowType,
        urlLoader = activity as? UrlLoader) {
      val nextUrl =
          when (it) {
            is Err -> {
              callback(Err(PaymentInternalError("Error processing payment")))
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
