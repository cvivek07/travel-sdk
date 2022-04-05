package com.ixigo.sdk.payment

import android.webkit.JavascriptInterface
import com.ixigo.sdk.common.*
import com.ixigo.sdk.common.NativePromiseError.Companion.sdkError
import com.ixigo.sdk.common.NativePromiseError.Companion.wrongInputError
import com.ixigo.sdk.payment.data.*
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.WebViewFragment
import com.ixigo.sdk.webview.WebViewFragmentListener
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal class PaymentJsInterface(
    private val webViewFragment: WebViewFragment,
    gatewayProvider: PaymentGatewayProvider
) : JsInterface, WebViewFragmentListener {
  override val name: String = "PaymentSDKAndroid"

  private val cachingGatewayProvider =
      CachingPaymentGatewayProvider(webViewFragment.requireActivity(), gatewayProvider)

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val inputAdapter by lazy { moshi.adapter(InitializeInput::class.java) }
  private val availableUpiAppsInputAdapter by lazy {
    moshi.adapter(GetAvailableUPIAppsInput::class.java)
  }
  private val availableUpiAppsResponseAdapter by lazy {
    moshi.adapter(GetAvailableUPIAppsResponse::class.java)
  }
  private val processUpiIntentInputAdapter by lazy {
    moshi.adapter(ProcessUpiIntentInput::class.java)
  }
  private val processUpiIntentResponseAdapter by lazy {
    moshi.adapter(ProcessUpiIntentResponse::class.java)
  }
  private val finishPaymentInputAdapter by lazy { moshi.adapter(FinishPaymentInput::class.java) }
  private val finishPaymentResponseAdapter by lazy {
    moshi.adapter(FinishPaymentResponse::class.java)
  }
  private val errorAdapter by lazy { moshi.adapter(NativePromiseError::class.java) }

  @JavascriptInterface
  fun initialize(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { inputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError", errorMessage = "Payment already initialized")
      returnError(error, errorPayload)
      return
    }

    gateway.initialize(input) {
      when (it) {
        is Err -> {
          val errorPayload =
              NativePromiseError(
                  errorCode = it.value.errorCode, errorMessage = it.value.errorMessage)
          returnError(error, errorPayload)
        }
        is Ok -> {
          executeResponse(replaceNativePromisePayload(success, "{}"))
        }
      }
    }
  }

  @JavascriptInterface
  fun getAvailableUPIApps(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { availableUpiAppsInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }

    gateway.listAvailableUPIApps(input) {
      when (it) {
        is Err -> {
          returnError(error, it.value)
        }
        is Ok -> {
          executeResponse(
              replaceNativePromisePayload(success, it.value, availableUpiAppsResponseAdapter))
        }
      }
    }
  }

  @JavascriptInterface
  fun processUPIIntent(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { processUpiIntentInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }

    gateway.processUpiIntent(input) {
      when (it) {
        is Err -> {
          returnError(error, it.value)
        }
        is Ok -> {
          executeResponse(
              replaceNativePromisePayload(success, it.value, processUpiIntentResponseAdapter))
        }
      }
    }
  }

  @JavascriptInterface
  fun finishPayment(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { finishPaymentInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    if (PaymentSDK.instance.finishPayment(input)) {
      webViewFragment.delegate?.onQuit()
      executeResponse(
          replaceNativePromisePayload(
              success,
              FinishPaymentResponse(handler = PaymentHandler.NATIVE),
              finishPaymentResponseAdapter))
    } else {
      returnError(error, sdkError("Unable to find transactionId=${input.transactionId}"))
    }
  }

  private fun returnError(error: String, errorPayload: NativePromiseError) {
    executeResponse(replaceNativePromisePayload(error, errorPayload, errorAdapter))
  }

  private fun executeResponse(message: String) {
    executeNativePromiseResponse(message, webViewFragment)
  }

  override fun onUrlLoadStart(webViewFragment: WebViewFragment, url: String) {
    cachingGatewayProvider.clear()
  }
}
