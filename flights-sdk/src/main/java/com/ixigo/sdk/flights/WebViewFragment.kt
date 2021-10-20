package com.ixigo.sdk.flights

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ixigo.sdk.flights.databinding.WebviewLayoutBinding
import com.ixigo.sdk.payment.PaymentInput
import kotlinx.parcelize.Parcelize

class WebViewFragment : Fragment() {
    private lateinit var binding: WebviewLayoutBinding
    private val webView get() = binding.webView
    val viewModel: WebViewViewModel by viewModels()

    lateinit var delegate: WebViewFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        viewModel.paymentResult.observe(this, { paymentResult ->
            // No action if Payment failed
            paymentResult.result.map {
                loadUrl(it.nextUrl)
            }
        })

        viewModel.loginResult.observe(this, { loginResult ->
            val url = loginResult.result.fold(
                onSuccess = {
                    loginResult.loginParams.successJSFunction.replace("AUTH_TOKEN", it.token)
                },
                onFailure = {
                    loginResult.loginParams.failureJSFunction
                })
            loadUrl(url)
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = WebviewLayoutBinding.inflate(layoutInflater)

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true

        addJavascriptInterface(IxiWebView(this))

        val initialPageData = arguments?.getParcelable<InitialPageData>(INITIAL_PAGE_DATA_ARGS)
        if (initialPageData != null) {
            webView.loadUrl(initialPageData.url, initialPageData.headers)
        }

        return binding.root
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
}

interface WebViewFragmentDelegate {
    fun quit()
}

@Parcelize
data class InitialPageData(
    val url: String,
    val headers: Map<String, String> = mapOf(),
) : Parcelable

interface JsInterface {
    val name: String
}

private class IxiWebView(val fragment: WebViewFragment) : JsInterface {
    override val name: String
        get() = "IxiWebView"

    @JavascriptInterface
    fun loginUser(logInSuccessJsFunction: String, logInFailureJsFunction: String): Boolean {
        return fragment.viewModel.login(
            LoginParams(
                successJSFunction = logInSuccessJsFunction,
                failureJSFunction = logInFailureJsFunction
            )
        )
    }

    @JavascriptInterface
    fun quit() {
        runOnUiThread {
            fragment.delegate.quit()
        }
    }

    @JavascriptInterface
    fun startNativePayment(paymentId: String?): Boolean {
        if (paymentId == null) {
            return false
        }
        return fragment.viewModel.startNativePayment(PaymentInput(paymentId))
    }

    fun runOnUiThread(runnable: Runnable) {
        fragment.activity?.runOnUiThread(runnable)
    }
}