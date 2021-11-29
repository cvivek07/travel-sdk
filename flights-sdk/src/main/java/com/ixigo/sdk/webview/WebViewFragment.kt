package com.ixigo.sdk.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.flights.databinding.WebviewLayoutBinding
import com.ixigo.sdk.payment.PaymentInput
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.parcelize.Parcelize

class WebViewFragment : Fragment() {
    private lateinit var binding: WebviewLayoutBinding
    @VisibleForTesting
    internal val webView get() = binding.webView
    val viewModel: WebViewViewModel by viewModels()

    lateinit var delegate: WebViewFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.paymentResult.observe(this, { paymentResult ->
            // No action if Payment failed
            paymentResult.result.onSuccess {
                loadUrl(it.nextUrl)
            }
        })

        viewModel.loginResult.observe(this, { loginResult ->
            val url = loginResult.result.mapBoth(
                {
                    loginResult.loginParams.successJSFunction.replace("AUTH_TOKEN", it.token)
                },
                {
                    loginResult.loginParams.failureJSFunction
                })
            loadUrl(url)
        })

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    delegate.quit()
                }
            }
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
}

interface WebViewFragmentDelegate {
    fun quit()
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

private class IxiWebView(val fragment: WebViewFragment) : JsInterface {

    private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    private val paymentInputAdapter by lazy { moshi.adapter(PaymentInput::class.java) }

    override val name: String
        get() = "IxiWebView"

    @JavascriptInterface
    fun loginUser(logInSuccessJsFunction: String, logInFailureJsFunction: String): Boolean {
        return fragment.viewModel.login(fragment.requireActivity(),
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
    fun executeNativePayment(paymentInfoString: String): Boolean {
        return try {
            val paymentInput = paymentInputAdapter.fromJson(paymentInfoString)!!
            fragment.viewModel.startNativePayment(fragment.requireActivity(), paymentInput)
        } catch (_: Exception)  {
            false
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        fragment.requireActivity().runOnUiThread(runnable)
    }
}