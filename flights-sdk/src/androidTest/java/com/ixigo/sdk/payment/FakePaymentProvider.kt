package com.ixigo.sdk.payment

import android.os.Handler

class FakePaymentProvider(private val result: PaymentResult? = null):PaymentProvider {
    override fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean {
        if (result == null) {
            return false
        }
        Handler().post {
            callback(result)
        }
        return true
    }
}