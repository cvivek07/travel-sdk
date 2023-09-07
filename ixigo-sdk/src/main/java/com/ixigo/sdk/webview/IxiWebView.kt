package com.ixigo.sdk.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.common.NativePromiseError
import com.ixigo.sdk.common.executeNativePromiseResponse
import com.ixigo.sdk.common.replaceNativePromisePayload
import com.ixigo.sdk.common.returnError
import com.ixigo.sdk.payment.PaymentCancelled
import com.ixigo.sdk.payment.PaymentError
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentInternalError
import com.ixigo.sdk.payment.PaymentSDK
import com.ixigo.sdk.payment.PaymentStatusResponse
import com.ixigo.sdk.payment.PaymentSuccessResult
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

class IxiWebView(
    private val fragment: WebViewFragment,
    private val ssoAuthProvider: SSOAuthProvider =
        SSOAuthProvider(IxigoSDK.instance.partnerTokenProvider),
    private val analyticsProvider: AnalyticsProvider = IxigoSDK.instance.analyticsProvider,
    private val viewModel: WebViewViewModel
) : JsInterface {

  private val moshi by lazy {
    Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(PaymentStatusResponse::class.java, "status")
                .withSubtype(PaymentSuccessResult::class.java, "success")
                .withSubtype(PaymentCancelled::class.java, "canceled")
                .withSubtype(PaymentInternalError::class.java, "error"))
        .add(
            PolymorphicJsonAdapterFactory.of(PaymentError::class.java, "error")
                .withSubtype(PaymentCancelled::class.java, "canceled")
                .withSubtype(PaymentInternalError::class.java, "internal error"))
        .add(KotlinJsonAdapterFactory())
        .build()
  }
  private val errorAdapter by lazy { moshi.adapter(NativePromiseError::class.java) }

  private val paymentInputAdapter by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(PaymentInput::class.java)
  }
  private val jsonPropertiesAdapter by lazy {
    Moshi.Builder()
        .add(PropertiesAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter<Map<String, String>>(
            Types.newParameterizedType(
                MutableMap::class.java, String::class.java, String::class.java))
        .lenient()
  }

  override val name: String
    get() = "IxiWebView"

  @JavascriptInterface
  fun loginUser(logInSuccessJsFunction: String, logInFailureJsFunction: String): Boolean {
    val activity = fragment.requireActivity()
    // TODO: get partnerId programmatically
    val partnerId = "iximaad"
    return ssoAuthProvider.login(activity, partnerId) { authResult ->
      activity.runOnUiThread {
        val url =
            authResult.mapBoth(
                { logInSuccessJsFunction.replace("AUTH_TOKEN", it.token) },
                { logInFailureJsFunction })
        fragment.loadUrl(url)
      }
    }
  }

  @JavascriptInterface
  fun quit() {
    PaymentSDK.instance.cancelPayment()
    fragment.delegate?.let { runOnUiThread { it.onQuit() } }
  }

  @JavascriptInterface
  fun executeNativePayment(paymentInfoString: String): Boolean {
    return try {
      val paymentInput = paymentInputAdapter.fromJson(paymentInfoString)!!
      viewModel.startNativePayment(fragment.requireActivity(), paymentInput)
    } catch (_: Exception) {
      false
    }
  }

  @JavascriptInterface
  fun executeNativePayment(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { paymentInputAdapter.fromJson(jsonInput) }.getOrNull()

    if (input == null) {
      returnError(error, NativePromiseError.wrongInputError(jsonInput))
      return
    }

    fragment.requireActivity().runOnUiThread {
      viewModel.startNativePaymentAsync(fragment.requireActivity(), input).observe(fragment) {
        it.result.onSuccess { paymentResponse ->
          executeNativePromiseResponse(
              replaceNativePromisePayload(
                  success,
                  PaymentSuccessResult(nextUrl = paymentResponse.nextUrl),
                  moshi.adapter(PaymentStatusResponse::class.java)),
              fragment)
        }

        it.result.onError { paymentError ->
          executeNativePromiseResponse(
              replaceNativePromisePayload(
                  success, paymentError, moshi.adapter(PaymentStatusResponse::class.java)),
              fragment)
        }
      }
    }
  }

  @JavascriptInterface
  fun openWindow(url: String, @Suppress("UNUSED_PARAMETER") title: String?) {
    runOnUiThread { IxigoSDK.instance.launchWebActivity(fragment.requireActivity(), url) }
  }

  @JavascriptInterface
  fun trackEvent(eventName: String, eventJson: String?) {
    runOnUiThread {
      analyticsProvider.logEvent(
          Event(
              name = eventName,
              properties = eventProperties(eventJson, null),
              referrer = fragment.webView.url))
    }
  }

  @JavascriptInterface
  fun trackEvent(analyticsServiceName: String, eventName: String, eventJson: String?) {
    runOnUiThread {
      analyticsProvider.logEvent(
          Event(
              name = eventName,
              properties = eventProperties(eventJson, analyticsServiceName),
              referrer = fragment.webView.url))
    }
  }

  @JavascriptInterface
  fun pwaReady() {
    runOnUiThread { fragment.pwaReady() }
  }

  private fun returnError(error: String, errorPayload: NativePromiseError) {
    returnError(error, errorPayload, errorAdapter, fragment)
  }

  private fun eventProperties(
      eventJson: String?,
      analyticsServiceName: String?
  ): Map<String, String> {
    val properties =
        if (analyticsServiceName != null) {
          mapOf("analyticsServiceName" to analyticsServiceName)
        } else {
          mapOf()
        }
    if (eventJson == null) {
      return properties
    }
    val parsedProperties =
        try {
          jsonPropertiesAdapter.fromJson(eventJson) ?: mapOf()
        } catch (e: Exception) {
          Timber.e(e, "Unable to parse Json event properties=${eventJson}")
          mapOf()
        }
    return properties + parsedProperties
  }

  private fun runOnUiThread(runnable: Runnable) {
    fragment.requireActivity().runOnUiThread(runnable)
  }
}

@Suppress("unused")
private class PropertiesAdapter {
  @ToJson
  @Keep
  fun toJson(properties: Map<String, String>): String {
    throw NotImplementedError()
  }

  @FromJson
  @Keep
  fun fromJson(properties: Map<String, Any?>): Map<String, String> {
    return properties.mapValues { it.value.toString() }
  }
}
