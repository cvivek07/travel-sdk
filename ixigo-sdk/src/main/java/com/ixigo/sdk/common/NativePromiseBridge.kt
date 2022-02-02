package com.ixigo.sdk.common

import com.ixigo.sdk.webview.WebViewFragment
import com.squareup.moshi.JsonAdapter

data class NativePromiseError(val errorCode: String?, val errorMessage: String? = null) {
  companion object {
    fun wrongInputError(wrongInput: String) =
        NativePromiseError(
            errorCode = "InvalidArgumentError",
            errorMessage = "unable to parse input=${wrongInput}")
  }
}

fun <T> replaceNativePromisePayload(message: String, payload: T, adapter: JsonAdapter<T>): String {
  return replaceNativePromisePayload(message, adapter.toJson(payload))
}

fun replaceNativePromisePayload(message: String, payload: String): String {
  return message.replace("TO_REPLACE_PAYLOAD", payload.replace("\"", "\\\""))
}

fun executeNativePromiseResponse(message: String, webViewFragment: WebViewFragment) {
  webViewFragment.requireActivity().runOnUiThread {
    webViewFragment.webView.evaluateJavascript(message, null)
  }
}
