package com.ixigo.sdk.payment

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result
import java.lang.Exception

interface PaymentProvider {
  fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean
}

@Generated data class PaymentResponse(val nextUrl: String)

@Generated @Keep data class PaymentInput(val product: String, val data: Map<String, String>)

typealias PaymentCallback = (PaymentResult) -> Unit

typealias PaymentResult = Result<PaymentResponse, PaymentError>

sealed class PaymentError

@Generated
@Keep
data class PaymentCancelled(val message: String = "Payment was canceled by the customer") :
    PaymentError()

@Generated
@Keep
data class PaymentInternalError(
    val message: String = "Error processing payment",
    val exception: Exception? = null
) : PaymentError()
