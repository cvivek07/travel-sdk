package com.ixigo.sdk.app

import android.os.Handler
import com.ixigo.sdk.payment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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