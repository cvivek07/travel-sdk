package com.ixigo.sdk.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.payment.PaymentInput
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

class IxiWebView(
    private val fragment: WebViewFragment,
    private val ssoAuthProvider: SSOAuthProvider =
        SSOAuthProvider(IxigoSDK.instance.partnerTokenProvider),
    private val analyticsProvider: AnalyticsProvider = IxigoSDK.instance.analyticsProvider
) : JsInterface {

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
    fragment.delegate?.let { runOnUiThread { it.onQuit() } }
  }

  @JavascriptInterface
  fun executeNativePayment(paymentInfoString: String): Boolean {
    return try {
      val paymentInput = paymentInputAdapter.fromJson(paymentInfoString)!!
      fragment.viewModel.startNativePayment(fragment.requireActivity(), paymentInput)
    } catch (_: Exception) {
      false
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
