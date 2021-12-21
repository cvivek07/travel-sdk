package com.ixigo.sdk.payment

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.ActivityResultHandler
import org.junit.Assert

interface ActivityResultPaymentProvider : PaymentProvider, ActivityResultHandler

class FakePaymentProvider(
    private val expectedActivity: Activity,
    private val results: Map<PaymentInput, PaymentResult>
) : PaymentProvider {
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean {
    Assert.assertSame(expectedActivity, activity)
    val result = results[input] ?: return false
    callback(result)
    return true
  }
}
