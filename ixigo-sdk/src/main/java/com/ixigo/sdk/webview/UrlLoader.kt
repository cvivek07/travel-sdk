package com.ixigo.sdk.webview

interface UrlLoader {
  fun loadUrl(url: String, headers: Map<String, String>? = null)
}
