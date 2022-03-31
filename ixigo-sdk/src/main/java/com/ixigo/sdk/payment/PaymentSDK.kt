package com.ixigo.sdk.payment

import android.content.Context
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.common.Result
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.payment.data.FinishPaymentInput
import com.ixigo.sdk.webview.*

/**
 * This is the main entrypoint to interact with Payments SDK.
 *
 * All interactions should happen via its singleton object, [PaymentSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [PaymentSDK.init(...)][init] once when you start-up your
 * Application.
 */
class PaymentSDK() : JsInterfaceProvider {

  private val currentTransactions: MutableMap<String, ProcessPaymentCallback> = mutableMapOf()

  fun processPayment(
      context: Context,
      transactionId: String,
      gatewayId: String = "1",
      config: FunnelConfig? = null,
      urlLoader: UrlLoader? = null,
      callback: ProcessPaymentCallback? = null,
  ) {
    callback?.let { currentTransactions[transactionId] = it }
    with(IxigoSDK.instance) {
      val url = getPaymentOptionsUrl(transactionId = transactionId, gatewayId = gatewayId)
      if (urlLoader != null) {
        urlLoader.loadUrl(url)
      } else {
        launchWebActivity(context, url, config)
      }
      analyticsProvider.logEvent(Event.with(action = "paymentsStartHome"))
    }
  }

  internal fun getPaymentOptionsUrl(transactionId: String, gatewayId: String = "1"): String =
      IxigoSDK.instance.getUrl(
          mapOf("page" to "PAYMENT", "gatewayId" to gatewayId, "txnId" to transactionId))

  internal fun finishPayment(input: FinishPaymentInput): Boolean {
    with(input) {
      val callback = currentTransactions[transactionId]
      return if (callback != null) {
        if (success) {
          callback(Ok(ProcessPaymentResponse(nextUrl)))
        } else {
          callback(Err(ProcessPaymentError(nextUrl)))
        }
        currentTransactions.remove(transactionId)
        true
      } else {
        false
      }
    }
  }

  companion object : SdkSingleton<PaymentSDK>("PaymentSDK") {

    /**
     * Initializes PaymentSDK with required parameters. This method needs to be called before
     * accessing the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     */
    @JvmStatic
    fun init(): PaymentSDK {
      PaymentSDK.assertIxigoSDKIsInitialized()
      PaymentSDK.assertNotCreated()

      val instance = PaymentSDK()
      PaymentSDK.INSTANCE = instance

      IxigoSDK.instance.webViewConfig.addJsInterfaceProvider(instance)
      IxigoSDK.instance.analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties = mapOf("sdk" to "payment", "sdkVersion" to BuildConfig.SDK_VERSION)))
      return instance
    }
  }

  override fun getJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface> {
    var jsInterfaces = mutableListOf<JsInterface>()
    if (url.startsWith(IxigoSDK.instance.config.apiBaseUrl) || url.startsWith("file://")) {
      jsInterfaces.add(PaymentJsInterface(webViewFragment))
    }
    return jsInterfaces
  }
}

typealias ProcessPaymentCallback = (ProcessPaymentResult) -> Unit

typealias ProcessPaymentResult = Result<ProcessPaymentResponse, ProcessPaymentError>

data class ProcessPaymentResponse(val nextUrl: String)

data class ProcessPaymentError(val nextUrl: String)
