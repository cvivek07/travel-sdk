package com.ixigo.sdk.common

import com.ixigo.sdk.webview.WebViewFragment
import com.squareup.moshi.JsonAdapter

data class NativePromiseError(
    val errorCode: String?,
    val errorMessage: String? = null,
    val debugMessage: String? = null
) {
  companion object {
    fun wrongInputError(wrongInput: String) =
        NativePromiseError(
            errorCode = "InvalidArgumentError",
            errorMessage = "unable to parse input=${wrongInput}")

    fun sdkError(message: String) =
        NativePromiseError(errorCode = "SDKError", errorMessage = message)

    fun notAvailableError(
        errorMessage: String? = "This functionality is not available on Android"
    ) = NativePromiseError(errorCode = "NotAvailableError", errorMessage = errorMessage)
  }
}

fun returnError(
    error: String,
    errorPayload: NativePromiseError,
    errorAdapter: JsonAdapter<NativePromiseError>,
    webViewFragment: WebViewFragment
) {
  executeResponse(replaceNativePromisePayload(error, errorPayload, errorAdapter), webViewFragment)
}

private fun executeResponse(message: String, webViewFragment: WebViewFragment) {
  executeNativePromiseResponse(message, webViewFragment)
}

fun <T> replaceNativePromisePayload(message: String, payload: T, adapter: JsonAdapter<T>): String {
  return replaceNativePromisePayload(message, adapter.toJson(payload))
}

fun replaceNativePromisePayload(message: String, payload: String): String {
  return message.replace("TO_REPLACE_PAYLOAD", payload.replace("\\", "\\\\").replace("\"", "\\\""))
}

fun executeNativePromiseResponse(message: String, webViewFragment: WebViewFragment) {
  if (webViewFragment.isAdded) {
    webViewFragment.requireActivity().runOnUiThread {
      webViewFragment.webView.evaluateJavascript(message, null)
    }
  }
}
