package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.webview.UrlLoader

/** PaymentProvider that will fulfill payments using PaymentSDK */
class PaymentSDKPaymentProvider : PaymentProvider {
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
        urlLoader = activity as? UrlLoader,
        tripId = input.data["tripId"],
        providerId = input.data["providerId"],
        productType = input.data["productType"]) {
      when (it) {
        is Err -> {
          callback(Err(PaymentInternalError("Error processing payment")))
          when (it.value) {
            is ProcessPaymentNotLoginError -> null
            is ProcessPaymentProcessingError -> {
              it.value.nextUrl
            }
          }
        }
        is Ok -> {
          callback(Ok(PaymentResponse(it.value.nextUrl)))
          it.value.nextUrl
        }
      }
    }

    return true
  }
}
