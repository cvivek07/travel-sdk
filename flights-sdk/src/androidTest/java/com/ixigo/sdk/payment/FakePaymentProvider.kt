package com.ixigo.sdk.payment

import android.os.Handler

class FakePaymentProvider(private val nextUrl: String? = null):PaymentProvider {
    override fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean {
        if (nextUrl == null) {
            return false
        }
        Handler().post {
            callback(PaymentResult.success(PaymentResponse(nextUrl)))
        }
        return true
    }
}