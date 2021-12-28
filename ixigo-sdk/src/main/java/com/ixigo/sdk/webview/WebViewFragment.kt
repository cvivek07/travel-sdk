package com.ixigo.sdk.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.databinding.WebviewLayoutBinding
import kotlinx.parcelize.Parcelize

class WebViewFragment : Fragment() {
  private lateinit var binding: WebviewLayoutBinding
  @VisibleForTesting
  internal val webView
    get() = binding.webView
  val viewModel: WebViewViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.paymentResult.observe(
        this,
        { paymentResult ->
          // No action if Payment failed
          paymentResult.result.onSuccess { loadUrl(it.nextUrl) }
        })

    viewModel.loginResult.observe(
        this,
        { loginResult ->
          val url =
              loginResult.result.mapBoth(
                  { loginResult.loginParams.successJSFunction.replace("AUTH_TOKEN", it.token) },
                  { loginResult.loginParams.failureJSFunction })
          loadUrl(url)
        })

    requireActivity().onBackPressedDispatcher.addCallback(webViewBackPressHandler)
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    super.onCreate(savedInstanceState)

    binding = WebviewLayoutBinding.inflate(layoutInflater)

    webView.webViewClient = CustomWebViewClient()
    webView.webChromeClient = WebChromeClient()
    webView.settings.javaScriptEnabled = true

    addJavascriptInterface(IxiWebView(this))

    val initialPageData = arguments?.getParcelable<InitialPageData>(INITIAL_PAGE_DATA_ARGS)
    if (initialPageData != null) {
      webView.loadUrl(initialPageData.url, initialPageData.headers)
    }

    return binding.root
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val activityResultHandler = IxigoSDK.getInstance().paymentProvider as ActivityResultHandler?
    activityResultHandler?.handle(requestCode, resultCode, data)
  }

  @SuppressLint("JavascriptInterface")
  private fun addJavascriptInterface(jsInterface: JsInterface) {
    webView.addJavascriptInterface(jsInterface, jsInterface.name)
  }

  private fun loadUrl(url: String, headers: Map<String, String> = mapOf()) {
    webView.loadUrl(url, headers)
  }

  companion object {
    const val INITIAL_PAGE_DATA_ARGS = "InitialPageData"
  }

  private val webViewBackPressHandler by lazy {
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() {
        webView.goBack()
      }
    }
  }

  private inner class CustomWebViewClient : WebViewClient() {

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
      super.doUpdateVisitedHistory(view, url, isReload)
      webViewBackPressHandler.isEnabled = view?.canGoBack() ?: false
      print(webViewBackPressHandler.isEnabled)
    }
  }
}

@Parcelize
@SuppressLint("ParcelCreator")
@Generated
data class InitialPageData(
    val url: String,
    val headers: Map<String, String> = mapOf(),
) : Parcelable

interface JsInterface {
  val name: String
}
