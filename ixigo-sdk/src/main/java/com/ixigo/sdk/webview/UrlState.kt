package com.ixigo.sdk.webview

class UrlState {

  private val uiConfigMap = mutableMapOf<String, UIConfig>()

  fun uiConfigForUrl(url: String): UIConfig? {
    return uiConfigMap[url]
  }

  fun updateUIConfig(url: String, config: UIConfig) {
    uiConfigMap[url] = config
  }
}
