package com.ixigo.sdk.payment

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentStatusResponseTest {
  private lateinit var moshi: Moshi

  @Before
  fun setup() {
    moshi =
        Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(PaymentStatusResponse::class.java, "status")
                    .withSubtype(PaymentSuccessResult::class.java, "success")
                    .withSubtype(PaymentCancelled::class.java, "canceled")
                    .withSubtype(PaymentInternalError::class.java, "error"))
            .add(KotlinJsonAdapterFactory())
            .build()
  }

  @Test
  fun `test success result serialization`() {
    val paymentSuccessResult = PaymentSuccessResult(nextUrl = "https://www.ixigo.com")
    val adapter = moshi.adapter(PaymentStatusResponse::class.java)
    val paymentSuccessResultJson = adapter.toJson(paymentSuccessResult)
    assertEquals(
        "{\"status\":\"success\",\"nextUrl\":\"https://www.ixigo.com\"}", paymentSuccessResultJson)
  }

  @Test
  fun `test canceled result serialization`() {
    val paymentCanceledResult = PaymentCancelled()
    val adapter = moshi.adapter(PaymentStatusResponse::class.java)
    val paymentCanceledResultJson = adapter.toJson(paymentCanceledResult)
    assertEquals(
        "{\"status\":\"canceled\",\"message\":\"Payment was canceled by the customer\"}",
        paymentCanceledResultJson)
  }

  @Test
  fun `test failure result serialization`() {
    val paymentFailureResult: PaymentError = PaymentInternalError()
    val adapter = moshi.adapter(PaymentStatusResponse::class.java)
    val paymentFailureResultJson = adapter.toJson(paymentFailureResult)
    assertEquals(
        "{\"status\":\"error\",\"message\":\"Error processing payment\"}", paymentFailureResultJson)
  }
}
