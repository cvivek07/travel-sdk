package com.ixigo.sdk.payment

import android.app.Activity
import android.os.Handler
import androidx.fragment.app.FragmentActivity

class FakePaymentProvider(private val result: PaymentResult? = null):PaymentProvider {
    override fun startPayment(activity: FragmentActivity, input: PaymentInput, callback: PaymentCallback): Boolean {
        if (result == null) {
            return false
        }
        Handler().post {
            callback(result)
        }
        return true
    }
}