package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock

class DisabledPaymentProviderTests {

  @Test
  fun `test is disabled`() {
    val activity: FragmentActivity = mock()
    val retValue =
        DisabledPaymentProvider.startPayment(activity, PaymentInput("product", mapOf())) {
          fail("Should not call callback")
        }
    assertFalse(retValue)
  }
}
