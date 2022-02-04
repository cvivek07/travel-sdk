package com.ixigo.sdk.webview

import android.webkit.JavascriptInterface
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.payment.PaymentInput
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IxiWebView(
    private val fragment: WebViewFragment,
    private val ssoAuthProvider: SSOAuthProvider =
        SSOAuthProvider(IxigoSDK.instance.partnerTokenProvider)
) : JsInterfaceRegexApply {

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val paymentInputAdapter by lazy { moshi.adapter(PaymentInput::class.java) }
  override val urlRegex: Regex
    get() = Regex("ixigo\\.com|abhibus\\.com|confirmtkt\\.com")

  override val name: String
    get() = "IxiWebView"

  @JavascriptInterface
  fun loginUser(logInSuccessJsFunction: String, logInFailureJsFunction: String): Boolean {
    val activity = fragment.requireActivity()
    ssoAuthProvider.login(activity) { authResult ->
      activity.runOnUiThread {
        val url =
            authResult.mapBoth(
                { logInSuccessJsFunction.replace("AUTH_TOKEN", it.token) },
                { logInFailureJsFunction })
        fragment.loadUrl(url)
      }
    }
    return true
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
  fun openWindow(url: String, @Suppress("UNUSED_PARAMETER") title: String) {
    runOnUiThread { IxigoSDK.instance.launchWebActivity(fragment.requireActivity(), url) }
  }

  private fun runOnUiThread(runnable: Runnable) {
    fragment.requireActivity().runOnUiThread(runnable)
  }
}
