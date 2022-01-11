package com.ixigo.sdk.bus

import android.webkit.JavascriptInterface
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.WebViewFragment

/**
 * JS Bridge for Abhibus PWA specific functionality.
 *
 * This class contains all injected methods that will be available to any Abhibus PWA
 *
 * @param webViewFragment
 */
class AbhiBusWebView(webViewFragment: WebViewFragment) : JsInterface {
  override val name: String
    get() = "AbhibusWebView"

  /**
   * Sample method to demonstrate how to add a JS bridge
   *
   * @return
   */
  @JavascriptInterface fun sayHello(): String = "Hello from Abhibus"
}
