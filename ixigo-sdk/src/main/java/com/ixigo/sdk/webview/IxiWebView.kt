package com.ixigo.sdk.webview

import android.webkit.JavascriptInterface
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.payment.PaymentInput
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IxiWebView(val fragment: WebViewFragment) : JsInterface {

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val paymentInputAdapter by lazy { moshi.adapter(PaymentInput::class.java) }

  override val name: String
    get() = "IxiWebView"

  @JavascriptInterface
  fun loginUser(logInSuccessJsFunction: String, logInFailureJsFunction: String): Boolean {
    return fragment.viewModel.login(
        fragment.requireActivity(),
        LoginParams(
            successJSFunction = logInSuccessJsFunction, failureJSFunction = logInFailureJsFunction))
  }

  @JavascriptInterface
  fun quit() {
    runOnUiThread { fragment.activity?.onBackPressed() }
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
    runOnUiThread { IxigoSDK.getInstance().launchWebActivity(fragment.requireActivity(), url) }
  }

  private fun runOnUiThread(runnable: Runnable) {
    fragment.requireActivity().runOnUiThread(runnable)
  }
}
