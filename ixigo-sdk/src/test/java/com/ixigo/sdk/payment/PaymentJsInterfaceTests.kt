package com.ixigo.sdk.payment

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.data.*
import com.ixigo.sdk.test.initializePaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewDelegate
import com.ixigo.sdk.webview.WebViewFragment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class PaymentJsInterfaceTests {

  @get:Rule val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private lateinit var fragment: WebViewFragment
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: ShadowWebView
  private lateinit var paymentJsInterface: PaymentJsInterface

  @Mock lateinit var mockWebViewDelegate: WebViewDelegate

  @Mock internal lateinit var justpayGateway: JusPayGateway

  @Before
  fun setup() {
    initializeTestIxigoSDK()
    initializePaymentSDK()
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      fragment.delegate = mockWebViewDelegate
      shadowWebView = Shadows.shadowOf(it.webView)
      paymentJsInterface = PaymentJsInterface(fragment, justpayGateway)
    }
  }

  @Test
  fun `test initialize works correctly`() {
    Mockito.`when`(
            justpayGateway.initialize(
                eq(
                    InitializeInput(
                        merchantId = "merchantIdValue",
                        customerId = "customerIdValue",
                        clientId = "clientIdValue")),
                any()))
        .then {
          val callback: InitializeCallback = it.getArgument(1)
          callback(Ok(Unit))
        }
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals("javascript:alert('success:{}')", shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test initialize throws error if already initialized`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        "javascript:alert('error:{\\\"errorCode\\\":\\\"InvalidArgumentError\\\",\\\"errorMessage\\\":\\\"Payment already initialized\\\"}')",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test initialize throws error if input is invalid`() {
    paymentJsInterface.initialize(
        "{}",
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={}\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test initialize throws error if juspay can not initialize`() {
    Mockito.`when`(
            justpayGateway.initialize(
                eq(
                    InitializeInput(
                        merchantId = "merchantIdValue",
                        customerId = "customerIdValue",
                        clientId = "clientIdValue")),
                any()))
        .then {
          val callback: InitializeCallback = it.getArgument(1)
          callback(Err(NativePromiseError(errorCode = "TestError")))
        }
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"TestError\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getAvailableUPIApps returns existing Apps`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    Mockito.`when`(
            justpayGateway.listAvailableUPIApps(
                eq(GetAvailableUPIAppsInput("orderIdValue", provider = "JUSPAY")), any()))
        .then {
          val callback: AvailableUPIAppsCallback = it.getArgument(1)
          callback(
              Ok(
                  GetAvailableUPIAppsResponse(
                      listOf(UpiApp(appName = "phonePe", packageName = "com.phonepe")))))
        }
    paymentJsInterface.getAvailableUPIApps(
        validGetAvailableUPIAppsInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"apps\":[{\"appName\":\"phonePe\",\"packageName\":\"com.phonepe\"}]}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getAvailableUPIApps throws error if juspay is not initialized`() {
    paymentJsInterface.getAvailableUPIApps(
        validGetAvailableUPIAppsInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"NotInitializedError\",\"errorMessage\":\"Call `PaymentSDKAndroid.initialize` before calling this method\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getAvailableUPIApps throws error for wrong Input`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    paymentJsInterface.getAvailableUPIApps(
        "{}",
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={}\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getAvailableUPIApps returns error if juspay returns error`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    Mockito.`when`(
            justpayGateway.listAvailableUPIApps(
                eq(GetAvailableUPIAppsInput(orderId = "orderIdValue", provider = "JUSPAY")), any()))
        .then {
          val callback: AvailableUPIAppsCallback = it.getArgument(1)
          callback(Err(NativePromiseError("Test Error")))
        }
    paymentJsInterface.getAvailableUPIApps(
        validGetAvailableUPIAppsInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"Test Error\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processUPIIntent works correctly`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    Mockito.`when`(justpayGateway.processUpiIntent(eq(validProcessUPIIntentInput), any())).then {
      val callback: ProcessUpiIntentCallback = it.getArgument(1)
      callback(Ok(ProcessUpiIntentResponse(orderId = "orderIdValue")))
    }
    paymentJsInterface.processUPIIntent(
        validProcessUPIIntentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"orderId\":\"orderIdValue\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processUPIIntent throws error if juspay is not initialized`() {
    paymentJsInterface.processUPIIntent(
        validProcessUPIIntentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"NotInitializedError\",\"errorMessage\":\"Call `PaymentSDKAndroid.initialize` before calling this method\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processUPIIntent throws error for wrong Input`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    paymentJsInterface.processUPIIntent(
        "{}",
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={}\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processUPIIntent returns error if juspay returns error`() {
    Mockito.`when`(justpayGateway.initialized).thenReturn(true)
    Mockito.`when`(justpayGateway.processUpiIntent(eq(validProcessUPIIntentInput), any())).then {
      val callback: ProcessUpiIntentCallback = it.getArgument(1)
      callback(Err(NativePromiseError("Test Error")))
    }
    paymentJsInterface.processUPIIntent(
        validProcessUPIIntentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"Test Error\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test json is correctly escaped`() {
    whenever(justpayGateway.initialize(eq(validInitializeInput), any())).then {
      val callback: ProcessUpiIntentCallback = it.getArgument(1)
      callback(Err(NativePromiseError("{\"jsonKey\": \"{\\\"innerKey\\\":\\\"innerValue\\\"}\"}")))
    }
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "JSON.parse('TO_REPLACE_PAYLOAD')")
    assertEquals(
        """JSON.parse('{\"errorCode\":\"{\\\"jsonKey\\\": \\\"{\\\\\\\"innerKey\\\\\\\":\\\\\\\"innerValue\\\\\\\"}\\\"}\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test finishPayment closes Funnel and calls processPayment for successful payment`() {
    val transactionId = "transactionIdValue"
    var processResult: ProcessPaymentResult? = null
    PaymentSDK.instance.processPayment(fragment.requireContext(), transactionId) {
      processResult = it
    }

    val nextUrl = "https://www.ixigo.com/payment/success"
    paymentJsInterface.finishPayment(
        validFinishPaymentInputString(),
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    verify(mockWebViewDelegate).onQuit()
    assertEquals(Ok(ProcessPaymentResponse(nextUrl)), processResult)
    assertEquals(
        """javascript:alert('success:{\"handler\":\"NATIVE\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test finishPayment closes Funnel and calls processPayment for failed payment`() {
    val transactionId = "transactionIdValue"
    var processResult: ProcessPaymentResult? = null
    PaymentSDK.instance.processPayment(fragment.requireContext(), transactionId) {
      processResult = it
    }

    val nextUrl = "https://www.ixigo.com/payment/success"
    paymentJsInterface.finishPayment(
        validFinishPaymentInputString(success = false),
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    verify(mockWebViewDelegate).onQuit()
    assertEquals(Err(ProcessPaymentError(nextUrl)), processResult)
    assertEquals(
        """javascript:alert('success:{\"handler\":\"NATIVE\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test finishPayment does nothing if transactionId is unknown`() {
    val transactionId = "transactionIdValue"
    var processResult: ProcessPaymentResult? = null
    PaymentSDK.instance.processPayment(fragment.requireContext(), transactionId) {
      processResult = it
    }

    paymentJsInterface.finishPayment(
        validFinishPaymentInputString(transactionId = "otherTransactionId"),
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    verify(mockWebViewDelegate, never()).onQuit()
    assertNull(processResult)
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"SDKError\",\"errorMessage\":\"Unable to find transactionId=otherTransactionId\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  private val validInitializeInput =
      InitializeInput(
          merchantId = "merchantIdValue",
          customerId = "customerIdValue",
          clientId = "clientIdValue")

  private val validInitializeInputString =
      """
      {
        "clientId": "clientIdValue",
        "customerId": "customerIdValue",
        "merchantId": "merchantIdValue",
        "provider": "JUSPAY"
      }
    """.trim()

  private val validGetAvailableUPIAppsInputString =
      """
      {
        "orderId": "orderIdValue",
        "provider": "JUSPAY"
      }
    """.trim()

  private val validProcessUPIIntentInputString =
      """
      {
        "provider": "JUSPAY",
        "orderId": "orderIdValue",
        "appPackage": "appPackageValue",
        "displayNote": "displayNoteValue",
        "clientAuthToken": "clientAuthTokenValue",
        "endUrls": ["endUrl1"],
        "amount": 102.3
      }
    """.trim()

  private val validProcessUPIIntentInput =
      ProcessUpiIntentInput(
          provider = "JUSPAY",
          orderId = "orderIdValue",
          appPackage = "appPackageValue",
          displayNote = "displayNoteValue",
          clientAuthToken = "clientAuthTokenValue",
          endUrls = listOf("endUrl1"),
          amount = 102.3)

  private fun validFinishPaymentInputString(
      transactionId: String = "transactionIdValue",
      success: Boolean = true
  ) =
      """
      {
        "transactionId": "$transactionId",
        "success": $success,
        "nextUrl": "https://www.ixigo.com/payment/success"
      }
    """.trim()
}
