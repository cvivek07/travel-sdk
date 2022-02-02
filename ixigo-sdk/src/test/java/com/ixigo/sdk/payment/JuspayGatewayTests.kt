package com.ixigo.sdk.payment

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.data.GetAvailableUPIAppsInput
import com.ixigo.sdk.payment.data.GetAvailableUPIAppsResponse
import com.ixigo.sdk.payment.data.UpiApp
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
            customerId = "customerIdValue")) { result = it }

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
            customerId = "customerIdValue")) { result = it }

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
    juspayGateway.listAvailableUPIApps(GetAvailableUPIAppsInput(orderId = "orderIdValue")) {
      result = it
    }

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
                            put("appPackage", "appPackageValue")
                          })
                    })
              })
          put("requestId", processJsonObject.getString("requestId"))
        },
        juspayResponseHandler)

    assertEquals(
        Ok(
            GetAvailableUPIAppsResponse(
                listOf(UpiApp(appPackage = "appPackageValue", appName = "appNameValue")))),
        result)
  }

  @Test
  fun `test listAvailableUPIApps throws error if hyperservices returns error`() {
    initializeHyperServices()

    var result: AvailableUPIAppsResult? = null
    juspayGateway.listAvailableUPIApps(GetAvailableUPIAppsInput(orderId = "orderIdValue")) {
      result = it
    }

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

  private fun initializeHyperServices() {
    juspayGateway.initialize(
        InitializeInput(
            merchantId = "merchantIdValue",
            clientId = "clientIdValue",
            customerId = "customerIdValue")) {}

    verify(hyperServices).initiate(capture(jsonObjectCaptor), capture(hyperCallbackCaptor))
  }
}
