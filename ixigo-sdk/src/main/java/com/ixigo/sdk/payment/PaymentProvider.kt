package com.ixigo.sdk.payment

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.NoCoverage
import com.ixigo.sdk.common.Result
import java.lang.Exception

interface PaymentProvider {
  fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean
}

@NoCoverage data class PaymentResponse(val nextUrl: String)

@NoCoverage @Keep data class PaymentInput(val product: String, val data: Map<String, String>)

typealias PaymentCallback = (PaymentResult) -> Unit

typealias PaymentResult = Result<PaymentResponse, PaymentError>

sealed class PaymentError

@NoCoverage
@Keep
data class PaymentCancelled(val message: String = "Payment was canceled by the customer") :
    PaymentError()

@NoCoverage
@Keep
data class PaymentInternalError(
    val message: String = "Error processing payment",
    val exception: Exception? = null
) : PaymentError()
