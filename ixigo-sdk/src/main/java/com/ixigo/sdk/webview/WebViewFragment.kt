package com.ixigo.sdk.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.databinding.WebviewLayoutBinding
import com.ixigo.sdk.ui.Failed
import com.ixigo.sdk.ui.Loaded
import com.ixigo.sdk.ui.Loading
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class WebViewFragment : Fragment(), UrlLoader {
  @VisibleForTesting internal lateinit var binding: WebviewLayoutBinding
  @VisibleForTesting
  internal val webView
    get() = binding.webView
  @VisibleForTesting
  internal val loadableView
    get() = binding.loadableView
  val viewModel: WebViewViewModel by viewModels()

  val analyticsProvider: AnalyticsProvider
    get() = IxigoSDK.instance.analyticsProvider

  val initialPageData: InitialPageData
    get() = arguments?.getParcelable(INITIAL_PAGE_DATA_ARGS)!!

  var delegate: WebViewDelegate? = null
  private lateinit var jsInterfaces: List<JsInterface>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.paymentResult.observe(
        this,
        { paymentResult ->
          // No action if Payment failed
          paymentResult.result.onSuccess { loadUrl(it.nextUrl) }
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
    loadableView.onGoBack = { activity?.onBackPressed() }
    loadableView.onRetry =
        {
          webView.reload()
          startedLoading()
        }

    webView.webViewClient = CustomWebViewClient()
    webView.webChromeClient = WebChromeClient()
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true

    jsInterfaces =
        IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(initialPageData.url, this)
    jsInterfaces.iterator().forEach(this::addJavascriptInterface)

    webView.loadUrl(initialPageData.url, initialPageData.headers)
    startedLoading()

    return binding.root
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val handlers =
        listOf(IxigoSDK.instance.paymentProvider, IxigoSDK.instance.partnerTokenProvider) +
            jsInterfaces
    handlers.forEach {
      (it as? ActivityResultHandler)?.apply { handle(requestCode, resultCode, data) }
    }
  }

  @SuppressLint("JavascriptInterface")
  private fun addJavascriptInterface(jsInterface: JsInterface) {
    webView.addJavascriptInterface(jsInterface, jsInterface.name)
  }

  internal fun loadUrl(url: String, headers: Map<String, String> = mapOf()) {
    webView.loadUrl(url, headers)
  }

  companion object {
    const val INITIAL_PAGE_DATA_ARGS = "InitialPageData"
    const val CONFIG = "WebViewFragmentConfig"
  }

  private val webViewBackPressHandler by lazy {
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() {
        webView.goBack()
      }
    }
  }

  private fun startedLoading(url: String = webView.url.toString()) {
    if (loadableView.status != Loading()) {
      loadableView.status = Loading()
      IxigoSDK.instance.uriIdlingResource.beginLoad(url)
    }
  }

  private fun stoppedLoading(url: String = webView.url.toString()) {
    with(IxigoSDK.instance.uriIdlingResource) {
      if (!isIdleNow) {
        IxigoSDK.instance.uriIdlingResource.endLoad(url)
      }
    }
  }

  private inner class CustomWebViewClient : WebViewClient() {

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
      super.doUpdateVisitedHistory(view, url, isReload)
      webViewBackPressHandler.isEnabled = view?.canGoBack() ?: false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)
      if (loadableView.status is Loading) {
        loadableView.status = Loaded
        stoppedLoading()
      }
      setStatusBarColorFromThemeColor()

      loadIxigoJsSDKIfNeeded()
    }

    private fun loadIxigoJsSDKIfNeeded() {
      webView.evaluateJavascript(
          """
        if (!window.IxigoSDK) {
          var script = document.createElement("script");
          script.type = "text/javascript";
          script.src = "https://rocket.ixigo.com/ixigo-js-sdk/latest/index.umd.js";
          document.body.appendChild(script);
        }
      """.trimIndent(),
          null)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
      if (request == null) {
        return false
      }
      val url = request.url.toString()
      return if (URLUtil.isNetworkUrl(url)) {
        startedLoading(url)
        analyticsProvider.logEvent(Event.with(action = "webviewStartLoad", referrer = url))
        false
      } else {
        analyticsProvider.logEvent(
            Event.with(action = "webviewStartAction", label = request.url.scheme, referrer = url))
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        true
      }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
      super.onReceivedHttpError(view, request, errorResponse)
      handleError(request, errorResponse?.statusCode.toString())
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
      super.onReceivedError(view, request, error)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        when (error?.errorCode) {
          ERROR_UNKNOWN ->
              // Unknown Errors can be Cache misses, for which we don't want to show an error view
              handleError(request, error?.toString(), showErrorViewIfNeeded = false)
          else -> handleError(request, error?.toString())
        }
      } else {
        handleError(request, error?.toString())
      }
    }

    private fun handleError(
        request: WebResourceRequest?,
        error: String?,
        showErrorViewIfNeeded: Boolean = true
    ) {
      if (request?.url.toString() == webView.url) {
        analyticsProvider.logEvent(
            Event.with(action = "webviewError", label = error, referrer = request?.url.toString()))
        if (showErrorViewIfNeeded) {
          loadableView.status = Failed()
          stoppedLoading()
        }
      }
    }

    private fun setStatusBarColorFromThemeColor() {
      webView.evaluateJavascript("document.querySelector('meta[name=\"theme-color\"]').content") {
        try {
          val color = Color.parseColor(it.replace("\"", ""))
          delegate?.updateStatusBarColor(color)
        } catch (e: Exception) {
          Timber.e(e, "Error trying to parse theme-color from value=$it")
        }
      }
    }
  }

  override fun loadUrl(url: String) {
    webView.loadUrl(url)
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

interface WebViewDelegate {
  fun onQuit()
  fun updateStatusBarColor(color: Int)
}
