package com.ixigo.sdk.payment

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentErrorTest {

  private lateinit var moshi: Moshi

  @Before
  fun setup() {
    moshi =
        Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(PaymentError::class.java, "error")
                    .withSubtype(PaymentCancelled::class.java, "canceled")
                    .withSubtype(PaymentInternalError::class.java, "internal error"))
            .add(KotlinJsonAdapterFactory())
            .build()
  }

  @Test
  fun `test subtype serialization`() {
    val paymentCanceled = PaymentCancelled()
    val paymentCanceledJson = moshi.adapter(PaymentError::class.java).toJson(paymentCanceled)
    assertEquals(
        "{\"error\":\"canceled\",\"message\":\"Payment was canceled by the customer\"}",
        paymentCanceledJson)
  }
}
