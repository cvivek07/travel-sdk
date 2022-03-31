package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.webview.WebActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class PaymentSDKProviderTests {

  lateinit var activity: FragmentActivity

  @Mock lateinit var webActivity: WebActivity

  @get:Rule val activityRule = ActivityScenarioRule(FragmentActivity::class.java)

  @get:Rule val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockPaymentSDK: PaymentSDK

  @Mock lateinit var mockIxigoSDK: IxigoSDK

  lateinit var provider: PaymentSDKPaymentProvider

  @Before
  fun setup() {
    provider = PaymentSDKPaymentProvider()
    IxigoSDK.replaceInstance(mockIxigoSDK)
    activityRule.scenario.onActivity { activity = it }
  }

  @Test
  fun `test startPayment call processPayment and opens nextUrl on success`() {
    val transactionId = "transactionIdValue"
    val nextUrl = "https://www.ixigo.com/payment/success"
    PaymentSDK.replaceInstance(mockPaymentSDK)
    whenever(
        mockPaymentSDK.processPayment(
            same(activity), eq(transactionId), eq("1"), eq(null), eq(null), any()))
        .then {
          val callback = it.getArgument(5) as ProcessPaymentCallback
          callback(Ok(ProcessPaymentResponse(nextUrl)))
          Unit
        }
    var paymentResult: PaymentResult? = null
    val ret =
        provider.startPayment(
            activity, PaymentInput("product", mapOf("paymentTransactionId" to transactionId))) {
          paymentResult = it
        }

    assertTrue(ret)
    assertEquals(Ok(PaymentResponse(nextUrl)), paymentResult)
    verify(mockIxigoSDK).launchWebActivity(activity, nextUrl)
  }

  @Test
  fun `test startPayment call processPayment and opens nextUrl on error`() {
    val transactionId = "transactionIdValue"
    val nextUrl = "https://www.ixigo.com/payment/success"
    PaymentSDK.replaceInstance(mockPaymentSDK)
    whenever(
        mockPaymentSDK.processPayment(
            same(activity), eq(transactionId), eq("1"), eq(null), eq(null), any()))
        .then {
          val callback = it.getArgument(5) as ProcessPaymentCallback
          callback(Err(ProcessPaymentError(nextUrl)))
          Unit
        }
    var paymentResult: PaymentResult? = null
    val ret =
        provider.startPayment(
            activity, PaymentInput("product", mapOf("paymentTransactionId" to transactionId))) {
          paymentResult = it
        }

    assertTrue(ret)
    assertEquals(false, paymentResult?.isSuccess)
    verify(mockIxigoSDK).launchWebActivity(activity, nextUrl)
  }

  @Test
  fun `test startPayment call processPayment with UrlLoader if activity implements UrlLoader`() {
    val transactionId = "transactionIdValue"
    val nextUrl = "https://www.ixigo.com/payment/success"
    PaymentSDK.replaceInstance(mockPaymentSDK)
    whenever(
        mockPaymentSDK.processPayment(
            same(webActivity),
            eq(transactionId),
            eq("1"),
            eq(null),
            urlLoader = same(webActivity),
            any()))
        .then {
          val callback = it.getArgument(5) as ProcessPaymentCallback
          callback(Ok(ProcessPaymentResponse(nextUrl)))
          Unit
        }
    var paymentResult: PaymentResult? = null
    val ret =
        provider.startPayment(
            webActivity, PaymentInput("product", mapOf("paymentTransactionId" to transactionId))) {
          paymentResult = it
        }

    assertTrue(ret)
    assertEquals(Ok(PaymentResponse(nextUrl)), paymentResult)
    verify(mockIxigoSDK).launchWebActivity(webActivity, nextUrl)
  }

  @Test
  fun `test startPayment returns false if paymentId is not present`() {
    var paymentResult: PaymentResult? = null
    val ret =
        provider.startPayment(activity, PaymentInput("product", mapOf())) { paymentResult = it }

    assertFalse(ret)
    assertNull(paymentResult)
    verifyNoInteractions(mockIxigoSDK)
  }
}
