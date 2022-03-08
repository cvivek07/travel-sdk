package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.common.Result
import com.ixigo.sdk.payment.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import `in`.juspay.hypersdk.core.PaymentConstants
import `in`.juspay.hypersdk.core.PaymentConstants.ENVIRONMENT.PRODUCTION
import `in`.juspay.hypersdk.core.PaymentConstants.ENVIRONMENT.SANDBOX
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter
import `in`.juspay.services.HyperServices
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

private typealias HyperServiceCallback = (payload: JSONObject) -> Unit

typealias AvailableUPIAppsResult = Result<GetAvailableUPIAppsResponse, NativePromiseError>

typealias AvailableUPIAppsCallback = (AvailableUPIAppsResult) -> Unit

typealias InitializeResult = Result<Unit, NativePromiseError>

typealias InitializeCallback = (InitializeResult) -> Unit

typealias ProcessUpiIntentResult = Result<ProcessUpiIntentResponse, NativePromiseError>

typealias ProcessUpiIntentCallback = (ProcessUpiIntentResult) -> Unit

internal class JusPayGateway(
    private val hyperInstance: HyperServices,
    ixigoSDK: IxigoSDK = IxigoSDK.instance
) {

  private val environment = if (ixigoSDK.config == Config.ProdConfig) PRODUCTION else SANDBOX
  constructor(fragmentActivity: FragmentActivity) : this(HyperServices(fragmentActivity))

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val juspayAvailableUpiAppsResponseAdapter by lazy {
    moshi.adapter(JuspayAvailableUPIAppsResponse::class.java)
  }

  private val requestMap: MutableMap<String, HyperServiceCallback> = mutableMapOf()

  val initialized: Boolean
    get() = hyperInstance.isInitialised

  private fun createRequestId(callback: HyperServiceCallback): String {
    val requestId = UUID.randomUUID().toString()
    requestMap[requestId] = callback
    return requestId
  }

  private fun executeCallback(data: JSONObject) {
    val requestId = data.optString("requestId")
    if (requestId != null && requestMap.containsKey(requestId)) {
      val callback = requestMap[requestId]!!
      callback(data)
      requestMap.remove(requestId)
    }
  }

  fun initialize(input: InitializeInput, callback: InitializeCallback) {
    val requestId = createRequestId { data ->
      val error = data.optBoolean("error")
      if (error) {
        callback(
            Err(
                NativePromiseError(
                    errorCode = data.optString("errorCode"),
                    errorMessage = data.optString("errorMessage"))))
      } else {
        callback(Ok(Unit))
      }
    }
    val payload =
        createJuspayRequestJsonPayload(createJuspayInitiationJsonPayload(input), requestId)

    hyperInstance.initiate(
        payload,
        object : HyperPaymentsCallbackAdapter() {
          override fun onEvent(data: JSONObject, juspayResponseHandler: JuspayResponseHandler) {
            when (data.getString("event")) {
              "show_loader" -> {}
              "hide_loader" -> {}
              "initiate_result" -> {
                executeCallback(data)
              }
              "process_result" -> {
                executeCallback(data)
              }
            }
          }
        })
  }

  data class UpiIntent(val app: String, val displayNote: String)

  fun listAvailableUPIApps(input: GetAvailableUPIAppsInput, callback: AvailableUPIAppsCallback) {
    val requestId = createRequestId { data ->
      val error = data.optBoolean("error")
      if (error) {
        callback(
            Err(
                NativePromiseError(
                    errorCode = data.optString("errorCode"),
                    errorMessage = data.optString("errorMessage"))))
      } else {
        val response =
            kotlin
                .runCatching {
                  juspayAvailableUpiAppsResponseAdapter.fromJson(data.getString("payload"))
                }
                .getOrNull()
        if (response == null) {
          callback(
              Err(
                  NativePromiseError(
                      errorCode = "SDKError",
                      errorMessage = "Error parsing juspay response=${data}")))
        } else {
          callback(Ok(GetAvailableUPIAppsResponse(response.availableApps)))
        }
      }
    }
    val payload = createListAvailableUpiAppsJsonPayload(input.orderId)
    hyperInstance.process(createJuspayRequestJsonPayload(payload, requestId))
  }

  private fun createListAvailableUpiAppsJsonPayload(orderId: String): JSONObject {
    return JSONObject().apply {
      put("action", "upiTxn")
      put("orderId", orderId)
      put("getAvailableApps", true)
    }
  }

  fun processUpiIntent(input: ProcessUpiIntentInput, callback: ProcessUpiIntentCallback) {
    val requestId = createRequestId { data ->
      val error = data.optBoolean("error")
      if (error) {
        callback(
            Err(
                NativePromiseError(
                    errorCode = data.optString("errorCode"),
                    errorMessage = data.optString("errorMessage"))))
      } else {
        callback(Ok(ProcessUpiIntentResponse(orderId = input.orderId)))
      }
    }
    hyperInstance.process(
        createJuspayRequestJsonPayload(createUpiIntentRequestPayload(input), requestId))
  }

  private fun createUpiIntentRequestPayload(input: ProcessUpiIntentInput): JSONObject {
    return with(input) {
      JSONObject().apply {
        put("action", "upiTxn")
        put("orderId", orderId)
        put("displayNote", displayNote)
        put("clientAuthToken", clientAuthToken)
        put(
            "endUrls",
            JSONArray().apply {
              for (endUrl in endUrls) {
                put(endUrl)
              }
            })
        put("upiSdkPresent", true)
        put("amount", amount.toString())
        put("payWithApp", appPackage)
      }
    }
  }

  internal fun createJuspayInitiationJsonPayload(input: InitializeInput): JSONObject {
    return JSONObject().apply {
      put("action", "initiate")
      put("merchantId", input.merchantId)
      put("clientId", input.clientId)
      put("customerId", input.customerId)
      put("merchantLoader", true)
      put(PaymentConstants.ENV, environment)
    }
  }

  private fun createJuspayRequestJsonPayload(
      paymentJsonPayload: JSONObject,
      requestId: String
  ): JSONObject {
    return JSONObject().apply {
      put("requestId", requestId)
      put("payload", paymentJsonPayload)
      put("service", "in.juspay.hyperapi")
    }
  }
}
