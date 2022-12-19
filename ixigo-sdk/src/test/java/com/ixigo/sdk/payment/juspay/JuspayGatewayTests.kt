package com.ixigo.sdk.payment.juspay

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.*
import com.ixigo.sdk.payment.data.*
import com.ixigo.sdk.payment.data.ProcessGatewayPaymentResponse
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter
import `in`.juspay.services.HyperServices
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class JuspayGatewayTests {

  @get:Rule val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private lateinit var fragment: WebViewFragment
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: ShadowWebView
  private lateinit var juspayGateway: JusPayGateway

  @Mock lateinit var hyperServices: HyperServices
  @Mock lateinit var juspayResponseHandler: JuspayResponseHandler

  @Captor lateinit var hyperCallbackCaptor: ArgumentCaptor<HyperPaymentsCallbackAdapter>
  @Captor lateinit var jsonObjectCaptor: ArgumentCaptor<JSONObject>

  @Before
  fun setup() {
    initializeTestIxigoSDK()
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      shadowWebView = Shadows.shadowOf(it.webView)
      juspayGateway = JusPayGateway(hyperServices)
    }
  }

  @Test
  fun `test initializes works correctly`() {
    var result: InitializeResult? = null
    juspayGateway.initialize(
        InitializeInput(
            merchantId = "merchantIdValue",
            clientId = "clientIdValue",
            customerId = "customerIdValue",
            provider = "providerValue",
            environment = null)) { result = it }

    verify(hyperServices).initiate(capture(jsonObjectCaptor), capture(hyperCallbackCaptor))

    val initiateJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"initiate","merchantId":"merchantIdValue","clientId":"clientIdValue","customerId":"customerIdValue","merchantLoader":true,"environment":"prod"}""",
        initiateJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", initiateJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "initiate_result")
          put("requestId", initiateJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(Ok(Unit), result)
  }

  @Test
  fun `test initializes throws error if initialization fails`() {
    var result: InitializeResult? = null
    juspayGateway.initialize(
        InitializeInput(
            merchantId = "merchantIdValue",
            clientId = "clientIdValue",
            customerId = "customerIdValue",
            provider = "providerValue",
            environment = null)) { result = it }

    verify(hyperServices).initiate(capture(jsonObjectCaptor), capture(hyperCallbackCaptor))

    val initiateJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"initiate","merchantId":"merchantIdValue","clientId":"clientIdValue","customerId":"customerIdValue","merchantLoader":true,"environment":"prod"}""",
        initiateJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", initiateJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "initiate_result")
          put("error", true)
          put("errorMessage", "errorMessageValue")
          put("errorCode", "errorCodeValue")
          put("requestId", initiateJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Err(NativePromiseError(errorCode = "errorCodeValue", errorMessage = "errorMessageValue")),
        result)
  }

  @Test
  fun `test listAvailableUPIApps returns list of UPI Apps`() {
    initializeHyperServices()

    var result: AvailableUPIAppsResult? = null
    juspayGateway.listAvailableUPIApps(
        GetAvailableUPIAppsInput(orderId = "orderIdValue", provider = "JUSPAY")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"upiTxn","orderId":"orderIdValue","getAvailableApps":true}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put(
              "payload",
              JSONObject().apply {
                put(
                    "availableApps",
                    JSONArray().apply {
                      put(
                          JSONObject().apply {
                            put("appName", "appNameValue")
                            put("packageName", "packageNameValue")
                          })
                    })
              })
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Ok(
            GetAvailableUPIAppsResponse(
                listOf(UpiApp(packageName = "packageNameValue", appName = "appNameValue")))),
        result)
  }

  @Test
  fun `test listAvailableUPIApps throws error if hyperservices returns error`() {
    initializeHyperServices()

    var result: AvailableUPIAppsResult? = null
    juspayGateway.listAvailableUPIApps(
        GetAvailableUPIAppsInput(orderId = "orderIdValue", provider = "JUSPAY")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"upiTxn","orderId":"orderIdValue","getAvailableApps":true}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("error", true)
          put("errorMessage", "errorMessageValue")
          put("errorCode", "errorCodeValue")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Err(NativePromiseError(errorCode = "errorCodeValue", errorMessage = "errorMessageValue")),
        result)
  }

  @Test
  fun `test processUpiIntent works correctly`() {
    initializeHyperServices()

    var result: ProcessGatewayPaymentResult? = null
    juspayGateway.processUpiIntent(
        ProcessUpiIntentInput(
            orderId = "orderIdValue",
            provider = "JUSPAY",
            appPackage = "appPackageValue",
            displayNote = "displayNodeValue",
            clientAuthToken = "clientAuthtokenValue",
            endUrls = listOf("endUrl1", "endUrl2"),
            amount = 100.23)) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"upiTxn","orderId":"orderIdValue","displayNote":"displayNodeValue","clientAuthToken":"clientAuthtokenValue","endUrls":["endUrl1","endUrl2"],"upiSdkPresent":true,"amount":"100.23","payWithApp":"appPackageValue"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(Ok(ProcessGatewayPaymentResponse(orderId = "orderIdValue")), result)
  }

  @Test
  fun `test processUpiIntent throws error if hyperservices returns error`() {
    initializeHyperServices()

    var result: ProcessGatewayPaymentResult? = null
    juspayGateway.processUpiIntent(
        ProcessUpiIntentInput(
            orderId = "orderIdValue",
            provider = "JUSPAY",
            appPackage = "appPackageValue",
            displayNote = "displayNodeValue",
            clientAuthToken = "clientAuthtokenValue",
            endUrls = listOf("endUrl1", "endUrl2"),
            amount = 100.23)) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"upiTxn","orderId":"orderIdValue","displayNote":"displayNodeValue","clientAuthToken":"clientAuthtokenValue","endUrls":["endUrl1","endUrl2"],"upiSdkPresent":true,"amount":"100.23","payWithApp":"appPackageValue"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("error", true)
          put("errorMessage", "errorMessageValue")
          put("errorCode", "errorCodeValue")
          put("payload", "payloadValue")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Err(
            NativePromiseError(
                errorCode = "errorCodeValue",
                errorMessage = "errorMessageValue",
                debugMessage = "payloadValue")),
        result)
  }

  @Test
  fun `test checkCredEligibility returns correctly`() {
    initializeHyperServices()

    var result: CredEligibilityResult? = null
    juspayGateway.checkCredEligibility(
        CredEligibilityInput(
            orderId = "orderIdValue",
            provider = "JUSPAY",
            amount = 100.0,
            customerMobile = "customerMobileValue",
            gatewayReferenceId = "gatewayReferenceIdValue")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"eligibility","data":{"apps":[{"mobile":"customerMobileValue","checkType":["cred"],"gatewayReferenceId":{"cred":"gatewayReferenceIdValue"}}]},"service":"in.juspay.hyperapi","orderId":"orderIdValue","amount":"100.0"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put(
              "payload",
              JSONObject().apply {
                put(
                    "apps",
                    JSONArray().apply {
                      put(
                          JSONObject().apply {
                            put(
                                "paymentMethodsEligibility",
                                JSONArray().apply {
                                  put(
                                      JSONObject().apply {
                                        put("isEligible", true)
                                        put("paymentMethod", "CRED")
                                      })
                                })
                          })
                    })
              })
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(Ok(CredEligibilityResponse(eligible = true)), result)
  }

  @Test
  fun `test checkCredEligibility throws error if hyperservices returns error`() {
    initializeHyperServices()

    var result: CredEligibilityResult? = null
    juspayGateway.checkCredEligibility(
        CredEligibilityInput(
            orderId = "orderIdValue",
            provider = "JUSPAY",
            amount = 100.0,
            customerMobile = "customerMobileValue",
            gatewayReferenceId = "gatewayReferenceIdValue")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"eligibility","data":{"apps":[{"mobile":"customerMobileValue","checkType":["cred"],"gatewayReferenceId":{"cred":"gatewayReferenceIdValue"}}]},"service":"in.juspay.hyperapi","orderId":"orderIdValue","amount":"100.0"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))

    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("error", true)
          put("errorMessage", "errorMessageValue")
          put("errorCode", "errorCodeValue")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Err(NativePromiseError(errorCode = "errorCodeValue", errorMessage = "errorMessageValue")),
        result)
  }

  @Test
  fun `test processCredPayment returns correctly`() {
    initializeHyperServices()

    var result: ProcessGatewayPaymentResult? = null
    juspayGateway.processCredPayment(
        ProcessCredPaymentInput(
            provider = "JUSPAY",
            orderId = "orderIdValue",
            amount = 100.0,
            customerMobile = "customerMobileValue",
            gatewayReferenceId = "gatewayReferenceIdValue",
            clientAuthToken = "clientAuthTokenValue")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"appPayTxn","orderId":"orderIdValue","paymentMethod":"CRED","amount":"100.0","application":"CRED","clientAuthToken":"clientAuthTokenValue","walletMobileNumber":"customerMobileValue"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(Ok(ProcessGatewayPaymentResponse(orderId = "orderIdValue")), result)
  }

  @Test
  fun `test processCredPayment throws error if hyperservices returns error`() {
    initializeHyperServices()

    var result: ProcessGatewayPaymentResult? = null
    juspayGateway.processCredPayment(
        ProcessCredPaymentInput(
            provider = "JUSPAY",
            orderId = "orderIdValue",
            amount = 100.0,
            customerMobile = "customerMobileValue",
            gatewayReferenceId = "gatewayReferenceIdValue",
            clientAuthToken = "clientAuthTokenValue")) { result = it }

    verify(hyperServices).process(capture(jsonObjectCaptor))

    verify(hyperServices).process(capture(jsonObjectCaptor))

    val processJsonObject = jsonObjectCaptor.value
    assertEquals(
        """{"action":"appPayTxn","orderId":"orderIdValue","paymentMethod":"CRED","amount":"100.0","application":"CRED","clientAuthToken":"clientAuthTokenValue","walletMobileNumber":"customerMobileValue"}""",
        processJsonObject.getString("payload"))
    assertEquals("in.juspay.hyperapi", processJsonObject.getString("service"))
    hyperCallbackCaptor.value.onEvent(
        JSONObject().apply {
          put("event", "process_result")
          put("error", true)
          put("errorMessage", "errorMessageValue")
          put("errorCode", "errorCodeValue")
          put("payload", "payloadValue")
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Err(
            NativePromiseError(
                errorCode = "errorCodeValue",
                errorMessage = "errorMessageValue",
                debugMessage = "payloadValue")),
        result)
  }

  private fun initializeHyperServices() {
    juspayGateway.initialize(
        InitializeInput(
            merchantId = "merchantIdValue",
            clientId = "clientIdValue",
            customerId = "customerIdValue",
            provider = "providerValue",
            environment = null)) {}

    verify(hyperServices).initiate(capture(jsonObjectCaptor), capture(hyperCallbackCaptor))
  }
}
