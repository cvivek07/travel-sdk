package com.ixigo.sdk.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
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
import com.ixigo.sdk.Handled
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.NotHandled
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.NoCoverage
import com.ixigo.sdk.databinding.WebviewLayoutBinding
import com.ixigo.sdk.ui.Failed
import com.ixigo.sdk.ui.Loaded
import com.ixigo.sdk.ui.Loading
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class WebViewFragment : Fragment(), UIConfigurable, UrlLoader {
  @VisibleForTesting internal lateinit var binding: WebviewLayoutBinding
  @VisibleForTesting
  internal val webView
    get() = binding.webView
  @VisibleForTesting
  internal val loadableView
    get() = binding.loadableView
  val viewModel: WebViewViewModel by viewModels()

  private val urlState = UrlState()

  private val defaultUIConfig = UIConfig(backNavigationMode = BackNavigationMode.Enabled())

  internal var uiConfig: UIConfig = defaultUIConfig
    private set

  val analyticsProvider: AnalyticsProvider
    get() = IxigoSDK.instance.analyticsProvider

  val initialPageData: InitialPageData
    get() = arguments?.getParcelable(INITIAL_PAGE_DATA_ARGS)!!

  var delegate: WebViewDelegate? = null
  private lateinit var jsInterfaces: List<JsInterface>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.paymentResult.observe(this) { paymentResult ->
      // No action if Payment failed
      paymentResult.result.onSuccess { loadUrl(it.nextUrl) }
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    super.onCreate(savedInstanceState)

    binding = WebviewLayoutBinding.inflate(layoutInflater)
    loadableView.onGoBack = { handleBackNavigation() }
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

    loadUrl(initialPageData.url, initialPageData.headers)
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

  internal fun pwaReady() {
    if (loadableView.status is Loading) {
      loadableView.status = Loaded
      stoppedLoading()
    }
  }

  @SuppressLint("JavascriptInterface")
  private fun addJavascriptInterface(jsInterface: JsInterface) {
    webView.addJavascriptInterface(jsInterface, jsInterface.name)
    (jsInterface as? WebViewFragmentListener?)?.let { addListener(it) }
  }

  override fun configUI(uiConfig: UIConfig) {
    this.uiConfig = uiConfig
    webView.url?.let { urlState.updateUIConfig(it, uiConfig) }
    updateCustombackPressHandler()
  }

  companion object {
    const val INITIAL_PAGE_DATA_ARGS = "InitialPageData"
    const val CONFIG = "WebViewFragmentConfig"
  }

  internal val webViewBackPressHandler by lazy {
    object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        when (uiConfig.backNavigationMode) {
          is BackNavigationMode.Handler -> {
            webView.evaluateJavascript("""javascript:IxigoSDK.ui.handleBackNavigation()""") {
              if (it != null && it.toBoolean()) {
                Timber.d("Back Navigation handled by PWA")
              } else {
                handleBackNavigation()
              }
            }
          }
          is BackNavigationMode.Enabled -> handleBackNavigation()
          is BackNavigationMode.Disabled -> Timber.d("Back Navigation ignored as it is disabled")
        }
      }
    }
  }

  /**
   * Only enable custom back navigation if:
   * - Mode is Disabled or Handler
   * - Mode is Enabled and we can goBack.
   */
  private fun updateCustombackPressHandler() {
    val customBackPressEnabled =
        when (uiConfig.backNavigationMode) {
          is BackNavigationMode.Enabled -> webView.canGoBack()
          else -> true
        }
    webViewBackPressHandler.isEnabled = customBackPressEnabled
  }

  private fun handleBackNavigation() {
    if (webView.canGoBack()) {
      webView.goBack()
    } else {
      delegate?.onQuit()
    }
  }

  private fun startedLoading(url: String = webView.url.toString()) {
    if (loadableView.status != Loading()) {
      loadableView.status = Loading(referrer = url)
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
      updateCustombackPressHandler()

      url?.let {
        val uiConfig = urlState.uiConfigForUrl(url) ?: defaultUIConfig
        configUI(uiConfig)
      }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)
      pwaReady()
      setStatusBarColorFromThemeColor()

      loadIxigoJsSDKIfNeeded()
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
      super.onPageStarted(view, url, favicon)

      for (listener in listeners) {
        listener.onUrlLoadStart(this@WebViewFragment, url)
      }
    }

    private fun loadIxigoJsSDKIfNeeded() {
      webView.evaluateJavascript(
          """
        if (!window.IxigoSDK) {
          var loadIxigoSDK = function() {
              var script = document.createElement("script");
              script.type = "text/javascript";
              script.src = "https://rocket.ixigo.com/ixigo-js-sdk/latest/index.umd.js";
              document.body.appendChild(script);
          }
          if (document.readyState === 'complete') {
              loadIxigoSDK();
          } else {
              window.addEventListener('load', loadIxigoSDK);
          }
        }
      """.trimIndent(),
          null)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
      if (request == null) {
        return false
      }
      val url = request.url.toString()
      IxigoSDK.instance.deeplinkHandler?.let {
        val uri = Uri.parse(url)
        if (uri != null) {
          context?.let { context ->
            when (it.handleUri(context, uri)) {
              is Handled -> {
                return true
              }
              NotHandled -> Unit
            }
          }
        }
      }
      return if (URLUtil.isNetworkUrl(url)) {
        startedLoading(url)
        analyticsProvider.logEvent(Event.with(action = "webviewStartLoad", referrer = url))
        false
      } else {
        analyticsProvider.logEvent(
            Event.with(action = "webviewStartAction", label = request.url.scheme, referrer = url))
        try {
          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
          true
        } catch (e: Exception) {
          Timber.e(e, "Unable to open activity for url=$url")
          false
        }
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
              handleError(request, error.toString(), showErrorViewIfNeeded = false)
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

  override fun loadUrl(url: String, headers: Map<String, String>?) {
    webView.loadUrl(url, headers ?: mapOf())
  }

  private val listeners: MutableList<WebViewFragmentListener> = mutableListOf()
  internal fun addListener(listener: WebViewFragmentListener) {
    listeners.add(listener)
  }
}

@Parcelize
@SuppressLint("ParcelCreator")
@NoCoverage
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

interface WebViewFragmentListener {
  fun onUrlLoadStart(webViewFragment: WebViewFragment, url: String?)
}
