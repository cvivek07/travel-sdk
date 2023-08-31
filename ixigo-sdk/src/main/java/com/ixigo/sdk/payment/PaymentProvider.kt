package com.ixigo.sdk.payment

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.NoCoverage
import com.ixigo.sdk.common.Result
import com.squareup.moshi.Json

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

@Keep
enum class PaymentStatus {
  Success,
  Error,
  Canceled
}

@Keep sealed class PaymentStatusResponse(@Json(name = "status") status: PaymentStatus)

@Keep
data class PaymentSuccessResult(val nextUrl: String) : PaymentStatusResponse(PaymentStatus.Success)

@Keep sealed class PaymentError(status: PaymentStatus) : PaymentStatusResponse(status)

@NoCoverage
@Keep
data class PaymentInternalError(
    val message: String = "Error processing payment",
) : PaymentError(PaymentStatus.Error)

@NoCoverage
@Keep
data class PaymentCancelled(val message: String = "Payment was canceled by the customer") :
    PaymentError(PaymentStatus.Canceled)
