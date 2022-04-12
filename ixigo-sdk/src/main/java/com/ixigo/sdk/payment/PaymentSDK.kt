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
class PaymentSDK(private val config: PaymentConfig) : JsInterfaceProvider {

  private val currentTransactions: MutableMap<String, ProcessPaymentCallback> = mutableMapOf()

  fun processPayment(
      context: Context,
      transactionId: String,
      gatewayId: String = "1",
      flowType: String = "PAYMENT_SDK",
      config: FunnelConfig? = null,
      urlLoader: UrlLoader? = null,
      callback: ProcessPaymentCallback? = null,
  ) {
    callback?.let { currentTransactions[transactionId] = it }
    with(IxigoSDK.instance) {
      val url =
          getPaymentOptionsUrl(
              transactionId = transactionId, gatewayId = gatewayId, flowType = flowType)
      if (urlLoader != null) {
        urlLoader.loadUrl(url, getHeaders(url))
      } else {
        launchWebActivity(context, url, config)
      }
      analyticsProvider.logEvent(Event.with(action = "paymentsStartHome"))
    }
  }

  private fun getPaymentOptionsUrl(
      transactionId: String,
      gatewayId: String = "1",
      flowType: String
  ): String =
      IxigoSDK.instance.getUrl(
          mapOf(
              "page" to "PAYMENT",
              "gatewayId" to gatewayId,
              "txnId" to transactionId,
              "flowType" to flowType))

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
    fun init(config: PaymentConfig = DefaultPaymentConfig): PaymentSDK {
      PaymentSDK.assertIxigoSDKIsInitialized()
      PaymentSDK.assertNotCreated()

      val instance = PaymentSDK(config)
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
      jsInterfaces.add(PaymentJsInterface(webViewFragment, DefaultPaymentGatewayProvider(config)))
    }
    return jsInterfaces
  }
}

data class PaymentConfig(val juspayConfig: JuspayConfig)

data class JuspayConfig(val environment: JusPayEnvironment)

val DefaultPaymentConfig =
    PaymentConfig(juspayConfig = JuspayConfig(environment = JusPayEnvironment.PRODUCTION))

typealias ProcessPaymentCallback = (ProcessPaymentResult) -> Unit

typealias ProcessPaymentResult = Result<ProcessPaymentResponse, ProcessPaymentError>

data class ProcessPaymentResponse(val nextUrl: String)

data class ProcessPaymentError(val nextUrl: String)
