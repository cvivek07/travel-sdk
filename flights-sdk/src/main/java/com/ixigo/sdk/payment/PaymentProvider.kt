package com.ixigo.sdk.payment

import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result

interface PaymentProvider {
    fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean
}

@Generated
data class PaymentResponse(val nextUrl: String)
@Generated
data class PaymentInput(val product: String, val data: Map<String, String>)

typealias PaymentCallback = (PaymentResult) -> Unit
typealias PaymentResult = Result<PaymentResponse>


