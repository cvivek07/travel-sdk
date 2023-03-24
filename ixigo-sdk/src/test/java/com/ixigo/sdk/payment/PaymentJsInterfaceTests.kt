package com.ixigo.sdk.payment

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.PackageManager.Companion.PHONEPE_PACKAGE_NAME
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowPackageManager
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

  @Mock internal lateinit var mockGatewayProvider: PaymentGatewayProvider
  @Mock internal lateinit var mockGateway: PaymentGateway
  @Mock internal lateinit var ssoAuthProvider: SSOAuthProvider
  private var context: Context? = null
  private var shadowPackageManager: ShadowPackageManager? = null

  @Before
  fun setup() {
    context = RuntimeEnvironment.application
    shadowPackageManager = shadowOf((context as Application?)!!.packageManager)
    initializeTestIxigoSDK()
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
              it.putBoolean(WebViewFragment.QUIT_PAYMENT_PAGE, true)
            })
    scenario.onFragment {
      fragment = it
      fragment.delegate = mockWebViewDelegate
      shadowWebView = Shadows.shadowOf(it.webView)
      paymentJsInterface = PaymentJsInterface(fragment, mockGatewayProvider)

      whenever(
              mockGatewayProvider.getPaymentGateway(eq("JUSPAY"), same(fragment.requireActivity())))
          .thenReturn(mockGateway)
      whenever(ssoAuthProvider.login(any(), any(), any())).then {
        val callback: AuthCallback = it.getArgument(2)
        callback.invoke(Ok(AuthData("token")))
        true
      }
    }
  }

  @Test
  fun `test initialize works correctly`() {
    Mockito.`when`(
            mockGateway.initialize(
                eq(
                    InitializeInput(
                        merchantId = "merchantIdValue",
                        customerId = "customerIdValue",
                        clientId = "clientIdValue",
                        provider = "JUSPAY",
                        environment = null)),
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        "javascript:alert('error:{\\\"errorCode\\\":\\\"InvalidArgumentError\\\",\\\"errorMessage\\\":\\\"Payment already initialized\\\"}')",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test gateways are reused across calls`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    for (i in 1..2) {
      paymentJsInterface.initialize(
          validInitializeInputString,
          "javascript:alert('success:TO_REPLACE_PAYLOAD')",
          "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    }
    verify(mockGatewayProvider, times(1)).getPaymentGateway("JUSPAY", fragment.requireActivity())
  }

  @Test
  fun `test gateways are NOT reused across calls when loading new page`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    paymentJsInterface.onUrlLoadStart(fragment, "https://www.ixigo.com/new-page")
    paymentJsInterface.initialize(
        validInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    verify(mockGatewayProvider, times(2)).getPaymentGateway("JUSPAY", fragment.requireActivity())
  }

  @Test
  fun `test initialize throws error for missing Provider`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.initialize(
        unknownProviderInitializeInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        "javascript:alert('error:{\\\"errorCode\\\":\\\"InvalidArgumentError\\\",\\\"errorMessage\\\":\\\"Could not find payment provider=UNKNOWN\\\"}')",
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
            mockGateway.initialize(
                eq(
                    InitializeInput(
                        merchantId = "merchantIdValue",
                        customerId = "customerIdValue",
                        clientId = "clientIdValue",
                        provider = "JUSPAY",
                        environment = null)),
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(
            mockGateway.listAvailableUPIApps(
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
  fun `test getAvailableUPIApps throws error for unknown provider`() {
    whenever(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.getAvailableUPIApps(
        unknownProviderGetAvailableUPIAppsInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"Could not find payment provider=Unknown\"}')""",
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(
            mockGateway.listAvailableUPIApps(
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(mockGateway.processUpiIntent(eq(validProcessUPIIntentInput), any())).then {
      val callback: ProcessGatewayPaymentCallback = it.getArgument(1)
      callback(Ok(ProcessGatewayPaymentResponse(orderId = "orderIdValue")))
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
  fun `test processUPIIntent throws error for unknown provider`() {
    whenever(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.processUPIIntent(
        unknownProviderProcessUPIIntentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"Could not find payment provider=Unknown\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processUPIIntent throws error for wrong Input`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
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
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(mockGateway.processUpiIntent(eq(validProcessUPIIntentInput), any())).then {
      val callback: ProcessGatewayPaymentCallback = it.getArgument(1)
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
    whenever(mockGateway.initialize(eq(validInitializeInput), any())).then {
      val callback: ProcessGatewayPaymentCallback = it.getArgument(1)
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
    PaymentSDK.instance.processPayment(fragment.requireActivity(), transactionId) {
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
    PaymentSDK.instance.processPayment(fragment.requireActivity(), transactionId) {
      processResult = it
    }

    val nextUrl = "https://www.ixigo.com/payment/success"
    paymentJsInterface.finishPayment(
        validFinishPaymentInputString(success = false),
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    verify(mockWebViewDelegate).onQuit()
    assertEquals(Err(ProcessPaymentProcessingError(nextUrl)), processResult)
    assertEquals(
        """javascript:alert('success:{\"handler\":\"NATIVE\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test finishPayment does nothing if transactionId is unknown`() {
    val transactionId = "transactionIdValue"
    var processResult: ProcessPaymentResult? = null
    PaymentSDK.instance.processPayment(fragment.requireActivity(), transactionId) {
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

  @Test
  fun `test checkCredEligibility returns eligibility as true if app is installed`() {
    shadowPackageManager!!.addPackage("com.dreamplug.androidapp")
    paymentJsInterface.checkCredEligibility(
        validCredEligibilityInput,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"eligible\":true}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test checkCredEligibility returns eligibility as false if app is not installed`() {
    paymentJsInterface.checkCredEligibility(
        validCredEligibilityInput,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"eligible\":false}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processCredPayment works correctly`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(mockGateway.processCredPayment(eq(validProcessCredPaymentInput), any())).then {
      val callback: ProcessGatewayPaymentCallback = it.getArgument(1)
      callback(Ok(ProcessGatewayPaymentResponse(orderId = "orderIdValue")))
    }
    paymentJsInterface.processCredPayment(
        validProcessCredPaymentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"orderId\":\"orderIdValue\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processCredPayment throws error if juspay is not initialized`() {
    paymentJsInterface.processCredPayment(
        validProcessCredPaymentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"NotInitializedError\",\"errorMessage\":\"Call `PaymentSDKAndroid.initialize` before calling this method\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processCredPayment throws error for unknown provider`() {
    whenever(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.processCredPayment(
        unknownProviderProcessCredPaymentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"Could not find payment provider=Unknown\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processCredPayment throws error for wrong Input`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    paymentJsInterface.processCredPayment(
        "{}",
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={}\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test processCredPayment returns error if juspay returns error`() {
    Mockito.`when`(mockGateway.initialized).thenReturn(true)
    Mockito.`when`(mockGateway.processCredPayment(eq(validProcessCredPaymentInput), any())).then {
      val callback: ProcessGatewayPaymentCallback = it.getArgument(1)
      callback(Err(NativePromiseError("Test Error")))
    }
    paymentJsInterface.processCredPayment(
        validProcessCredPaymentInputString,
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"Test Error\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test scanCard returns NotImplementedError`() {
    paymentJsInterface.scanCreditCard(
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('error:{\"errorCode\":\"NotAvailableError\",\"errorMessage\":\"This functionality is not available on Android\"}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test isPhonePeUpiAvailable returns true if app is installed`() {
    shadowPackageManager!!.addPackage(PHONEPE_PACKAGE_NAME)
    paymentJsInterface.isPhonePeUpiAvailable(
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"available\":true}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test isPhonePeUpiAvailable returns false if app is not installed`() {
    paymentJsInterface.isPhonePeUpiAvailable(
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"available\":false}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getPhonePeVersionCode returns version code if app is installed`() {
    shadowPackageManager!!.addPackage(PHONEPE_PACKAGE_NAME)
    paymentJsInterface.getPhonePeVersionCode(
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"versionCode\":0}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test getPhonePeVersionCode returns -1 if app is not installed`() {
    paymentJsInterface.getPhonePeVersionCode(
        "javascript:alert('success:TO_REPLACE_PAYLOAD')",
        "javascript:alert('error:TO_REPLACE_PAYLOAD')")
    assertEquals(
        """javascript:alert('success:{\"versionCode\":-1}')""",
        shadowWebView.lastEvaluatedJavascript)
  }

  private val validInitializeInput =
      InitializeInput(
          merchantId = "merchantIdValue",
          customerId = "customerIdValue",
          clientId = "clientIdValue",
          provider = "JUSPAY",
          environment = null)

  private val validInitializeInputString =
      """
      {
        "clientId": "clientIdValue",
        "customerId": "customerIdValue",
        "merchantId": "merchantIdValue",
        "provider": "JUSPAY"
      }
    """.trim()

  private val unknownProviderInitializeInputString =
      """
      {
        "clientId": "clientIdValue",
        "customerId": "customerIdValue",
        "merchantId": "merchantIdValue",
        "provider": "UNKNOWN"
      }
    """.trim()

  private val validGetAvailableUPIAppsInputString =
      """
      {
        "orderId": "orderIdValue",
        "provider": "JUSPAY"
      }
    """.trim()

  private val validCredEligibilityInput =
      """
      {
        "orderId": "orderIdValue",
        "provider": "JUSPAY",
        "amount": 100.0,
        "gatewayReferenceId": "gatewayReferenceIdValue",
        "customerMobile": "1234567890"
      }
    """.trim()

  private val unknownProviderGetAvailableUPIAppsInputString =
      """
      {
        "orderId": "orderIdValue",
        "provider": "Unknown"
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

  private val unknownProviderProcessUPIIntentInputString =
      """
      {
        "provider": "Unknown",
        "orderId": "orderIdValue",
        "appPackage": "appPackageValue",
        "displayNote": "displayNoteValue",
        "clientAuthToken": "clientAuthTokenValue",
        "endUrls": ["endUrl1"],
        "amount": 102.3
      }
    """.trim()

  private val validProcessCredPaymentInputString =
      """
      {
        "provider": "JUSPAY",
        "orderId": "orderIdValue",
        "clientAuthToken": "clientAuthTokenValue",
        "gatewayReferenceId": "gatewayReferenceIdValue",
        "customerMobile": "1234567890",
        "amount": 102.3
      }
    """.trim()

  private val unknownProviderProcessCredPaymentInputString =
      """
      {
        "provider": "Unknown",
        "orderId": "orderIdValue",
        "clientAuthToken": "clientAuthTokenValue",
        "gatewayReferenceId": "gatewayReferenceIdValue",
        "customerMobile": "1234567890",
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

  private val validProcessCredPaymentInput =
      ProcessCredPaymentInput(
          orderId = "orderIdValue",
          clientAuthToken = "clientAuthTokenValue",
          gatewayReferenceId = "gatewayReferenceIdValue",
          amount = 102.3,
          customerMobile = "1234567890",
          provider = "JUSPAY")

  private val validPhonePeRedirectInputString =
      """
      {
        "redirectType": "INTENT",
        "redirectUrl": "upi://pay?pa=IXIGOUAT@ybl&pn=IXIGO&am=806.00&mam=806.00&tr=8DDZIQ1H8W1DVBYZF&tn=Payment+for+8DDZIQ1H8W1DVBYZF&mc=5311&mode=04&purpose=00&utm_campaign=DEBIT&utm_medium=IXIGOUAT&utm_source=8DDZIQ1H8W1DVBYZF"
      }
    """.trim()

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
