package com.ixigo.sdk.webview

import android.webkit.WebStorage

internal class WebViewConfig(val webStorage: WebStorage = WebStorage.getInstance()) {

  private var providers = mutableListOf<JsInterfaceProvider>()

  fun addJsInterfaceProvider(provider: JsInterfaceProvider) {
    providers.add(provider)
  }

  fun getMatchingJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface> {
    return providers.map { it.getJsInterfaces(url, webViewFragment) }.flatten()
  }
}

interface JsInterfaceProvider {
  fun getJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface>
}
