package com.ixigo.sdk.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.PartnerTokenError
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

class HtmlOutJsInterface(
    private val webViewFragment: WebViewFragment,
    private val partnerTokenProvider: PartnerTokenProvider = IxigoSDK.instance.partnerTokenProvider
) : JsInterface {
  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val ssoInputAdapter by lazy { moshi.adapter(SSOInput::class.java) }
  private val ssoResultAdapter by lazy { moshi.adapter(SSOResult::class.java) }

  override val name: String
    get() = "HTMLOUT"

  @JavascriptInterface
  fun invokeSSOLogin(jsonInput: String) {
    val ssoInput = kotlin.runCatching { ssoInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (ssoInput == null) {
      Timber.e("Error parsing SSO json=$jsonInput")
      return
    }
    partnerTokenProvider.fetchPartnerToken(
        webViewFragment.requireActivity(), PartnerTokenProvider.Requester.CUSTOMER) {
      when (it) {
        is Ok -> {
          Timber.i("Successfully fetched partnerToken=${it.value.token}")
          val ssoResult = SSOResult(ssoInput.promiseId, SSOResultData.success(it.value.token))
          executeSSOCallback(ssoInput, ssoResult)
        }
        is Err -> {
          Timber.e("Error fetching partnerToken=${it.value}")
          val ssoResult = SSOResult(ssoInput.promiseId, SSOResultData.error(it.value))
          executeSSOCallback(ssoInput, ssoResult)
        }
      }
    }
  }

  @JavascriptInterface
  fun quit() {
    webViewFragment.delegate?.let { webViewFragment.activity?.runOnUiThread { it.onQuit() } }
  }

  private fun executeSSOCallback(ssoInput: SSOInput, ssoResult: SSOResult) {
    val ssoResultJson = ssoResultAdapter.toJson(ssoResult)
    webViewFragment.activity?.runOnUiThread {
      webViewFragment.webView.loadUrl("javascript:{${ssoInput.callBack}('${ssoResultJson}');};")
    }
  }

  @Keep
  data class SSOInput(
      @Json(name = "callBack") val callBack: String,
      @Json(name = "provider") val provider: String,
      @Json(name = "promiseId") val promiseId: String
  )
  @Keep
  data class SSOResult(
      @Json(name = "promiseId") val promiseId: String,
      @Json(name = "data") val data: SSOResultData
  )
  @Keep
  data class SSOResultData(
      @Json(name = "responseCode") val responseCode: Int,
      @Json(name = "grantToken") val grantToken: String?,
      @Json(name = "errorMessage") val errorMessage: String?
  ) {
    companion object {
      fun success(grantToken: String): SSOResultData {
        return SSOResultData(responseCode = 200, grantToken = grantToken, errorMessage = null)
      }

      fun error(error: PartnerTokenError): SSOResultData {
        return SSOResultData(
            responseCode = error.code, grantToken = null, errorMessage = error.message)
      }
    }
  }
}
