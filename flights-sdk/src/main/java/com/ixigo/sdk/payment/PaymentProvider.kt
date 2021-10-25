package com.ixigo.sdk.payment

import com.ixigo.sdk.common.Result

interface PaymentProvider {
    fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean
}

data class PaymentResponse(val nextUrl: String)
data class PaymentInput(val paymentId: String)

typealias PaymentCallback = (PaymentResult) -> Unit
typealias PaymentResult = Result<PaymentResponse>


