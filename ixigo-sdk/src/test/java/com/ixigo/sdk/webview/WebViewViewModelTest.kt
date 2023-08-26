package com.ixigo.sdk.webview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.PaymentCallback
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentInternalError
import com.ixigo.sdk.payment.PaymentProvider
import com.ixigo.sdk.payment.PaymentResponse
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq

@RunWith(AndroidJUnit4::class)
class WebViewViewModelTest {

  private lateinit var viewmodel: WebViewViewModel

  @Mock lateinit var mockSdk: IxigoSDK

  @Mock lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Mock lateinit var mockPaymentProvider: PaymentProvider

  @Mock lateinit var mockActivity: FragmentActivity

  @JvmField @Rule val instantExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Mockito.`when`(mockSdk.analyticsProvider).thenReturn(mockAnalyticsProvider)
    Mockito.`when`(mockSdk.paymentProvider).thenReturn(mockPaymentProvider)
    viewmodel = WebViewViewModel(mockSdk)
  }

  @Test
  fun `test startNativePayment logs paymentStarted Event`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    viewmodel.startNativePayment(mockActivity, paymentInput)
    Mockito.verify(mockAnalyticsProvider)
        .logEvent(argThat { event -> event.name == "paymentStart" })
  }

  @Test
  fun `test startNativePaymentAsync logs paymentStarted Event`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    viewmodel.startNativePaymentAsync(mockActivity, paymentInput)
    Mockito.verify(mockAnalyticsProvider)
        .logEvent(argThat { event -> event.name == "paymentStart" })
  }

  @Test
  fun `test startNativePayment logs paymentFinish event post payment`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    viewmodel.startNativePayment(mockActivity, paymentInput)

    val callbackCaptor = argumentCaptor<PaymentCallback>()
    Mockito.verify(mockPaymentProvider)
        .startPayment(any(), eq(paymentInput), callbackCaptor.capture())

    val paymentCallback = callbackCaptor.lastValue
    val successPaymentResult = Ok(PaymentResponse("https://ixigo.com/nextUrl"))
    paymentCallback.invoke(successPaymentResult)

    Mockito.verify(mockAnalyticsProvider)
        .logEvent(argThat { event -> event.name == "paymentFinished" })
  }

  @Test
  fun `test startNativePaymentAsync logs paymentFinish event post payment`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    viewmodel.startNativePayment(mockActivity, paymentInput)

    val callbackCaptor = argumentCaptor<PaymentCallback>()
    Mockito.verify(mockPaymentProvider)
        .startPayment(any(), eq(paymentInput), callbackCaptor.capture())

    val paymentCallback = callbackCaptor.lastValue
    val successPaymentResult = Ok(PaymentResponse("https://ixigo.com/nextUrl"))
    paymentCallback.invoke(successPaymentResult)

    Mockito.verify(mockAnalyticsProvider)
        .logEvent(argThat { event -> event.name == "paymentFinished" })
  }

  @Test
  fun `test startNativePayment sets payment result post payment`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    viewmodel.startNativePayment(mockActivity, paymentInput)

    val callbackCaptor = argumentCaptor<PaymentCallback>()
    Mockito.verify(mockPaymentProvider)
        .startPayment(any(), eq(paymentInput), callbackCaptor.capture())

    val paymentCallback = callbackCaptor.lastValue
    val successPaymentResult = Ok(PaymentResponse("https://ixigo.com/nextUrl"))
    paymentCallback.invoke(successPaymentResult)

    val expectedPaymentResult = NativePaymentResult(paymentInput, successPaymentResult)
    val actualPaymentResult = viewmodel.paymentResult.value
    assert(expectedPaymentResult == actualPaymentResult)
  }

  @Test
  fun `test startNativePaymentAsync sets payment result post payment`() {
    val paymentInput = PaymentInput("flights", emptyMap())
    val paymentResult = viewmodel.startNativePaymentAsync(mockActivity, paymentInput)

    val callbackCaptor = argumentCaptor<PaymentCallback>()
    Mockito.verify(mockPaymentProvider)
        .startPayment(any(), eq(paymentInput), callbackCaptor.capture())

    val paymentCallback = callbackCaptor.lastValue
    val successPaymentResult = Ok(PaymentResponse("https://ixigo.com/nextUrl"))
    paymentCallback.invoke(successPaymentResult)

    val expectedPaymentResult = NativePaymentResult(paymentInput, successPaymentResult)
    val actualPaymentResult = paymentResult.value
    assert(expectedPaymentResult == actualPaymentResult)
  }

  @Test
  fun `test startNativePaymentAsync sets payment result error for incorrect payment input`() {
    Mockito.`when`(mockPaymentProvider.startPayment(any(), any(), any())).thenReturn(false)

    val paymentInput = PaymentInput("flights", emptyMap())
    val paymentResult = viewmodel.startNativePaymentAsync(mockActivity, paymentInput)

    val expectedPaymentResult =
        NativePaymentResult(paymentInput, Err(PaymentInternalError("Incorrect input")))
    val actualPaymentResult = paymentResult.value

    assert(expectedPaymentResult == actualPaymentResult)
  }
}
