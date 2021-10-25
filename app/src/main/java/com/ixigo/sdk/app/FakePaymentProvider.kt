package com.ixigo.sdk.app

import android.os.Handler
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.*

class FakePaymentProvider(private val nextUrl: String? = null):PaymentProvider {
    override fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean {
        if (nextUrl == null) {
            return false
        }
        Handler().post {
            callback(Ok(PaymentResponse(nextUrl)))
        }
        return true
    }
}