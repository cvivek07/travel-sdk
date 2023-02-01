package com.ixigo.sdk.payment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.common.*
import com.ixigo.sdk.payment.PaymentSDK.Companion.init
import com.ixigo.sdk.payment.data.FinishPaymentInput
import com.ixigo.sdk.webview.*
import timber.log.Timber

/**
 * This is the main entrypoint to interact with Payments SDK.
 *
 * All interactions should happen via its singleton object, [PaymentSDK.getInstance()][getInstance].
 *
 * Before using it, you need to call [PaymentSDK.init(...)][init] once when you start-up your
 * Application.
 */
class PaymentSDK(
    private val config: PaymentConfig,
    private val ssoAuthProvider: SSOAuthProvider =
        SSOAuthProvider(IxigoSDK.instance.partnerTokenProvider)
) : JsInterfaceProvider {

  private val currentTransactions: MutableMap<String, ProcessPaymentCallback> = mutableMapOf()

  fun processPayment(
      activity: FragmentActivity,
      transactionId: String,
      tripId: String? = null,
      providerId: String? = null,
      productType: String? = null,
      gatewayId: String = "1",
      flowType: String = "PAYMENT_SDK",
      config: FunnelConfig? = null,
      urlLoader: UrlLoader? = null,
      callback: ProcessPaymentCallback? = null,
  ) {
    ssoAuthProvider.login(activity, IxigoSDK.instance.appInfo.clientId) { authResult ->
      when (authResult) {
        is Err -> {
          Timber.e("Unable to perform login before payment. Error=${authResult.value}")
          callback?.let { it.invoke(Err(ProcessPaymentNotLoginError(authResult.value))) }
        }
        is Ok -> {
          callback?.let { currentTransactions[transactionId] = it }
          with(IxigoSDK.instance) {
            val url =
                getPaymentOptionsUrl(
                    transactionId = transactionId,
                    gatewayId = gatewayId,
                    flowType = flowType,
                    tripId = tripId,
                    providerId = providerId,
                    productType = productType)
            val authHeaders = mapOf("Authorization" to authResult.value.token)
            if (urlLoader != null) {
              urlLoader.loadUrl(url, authHeaders + getHeaders(url))
            } else {
              launchWebActivity(activity, url, config, headers = authHeaders)
            }
            analyticsProvider.logEvent(Event.with(action = "paymentsStartHome"))
          }
        }
      }
    }
  }

  fun getPaymentFragment(
      activity: FragmentActivity,
      transactionId: String,
      tripId: String? = null,
      providerId: String? = null,
      productType: String? = null,
      gatewayId: String = "1",
      flowType: String = "PAYMENT_SDK",
      config: FunnelConfig? = null,
      urlLoader: UrlLoader? = null,
      quitPaymentPage: Boolean,
      callback: ProcessPaymentCallback? = null
  ): WebViewFragment {
    val webViewFragment = WebViewFragment()
    ssoAuthProvider.login(activity, IxigoSDK.instance.appInfo.clientId) { authResult ->
      when (authResult) {
        is Err -> {
          Timber.e("Unable to perform login before payment. Error=${authResult.value}")
          callback?.let { it.invoke(Err(ProcessPaymentNotLoginError(authResult.value))) }
        }
        is Ok -> {
          callback?.let { currentTransactions[transactionId] = it }
          with(IxigoSDK.instance) {
            val url =
                getPaymentOptionsUrl(
                    transactionId = transactionId,
                    gatewayId = gatewayId,
                    flowType = flowType,
                    tripId = tripId,
                    providerId = providerId,
                    productType = productType)
            val authHeaders = mapOf("Authorization" to authResult.value.token)
            if (urlLoader != null) {
              urlLoader.loadUrl(url, authHeaders + getHeaders(url))
            } else {
              val bundle = Bundle()
              bundle.putParcelable(
                  WebViewFragment.INITIAL_PAGE_DATA_ARGS,
                  InitialPageData(url, authHeaders + getHeaders(url)))
              config?.let { bundle.putParcelable(WebViewFragment.CONFIG, it) }
              bundle.putBoolean(WebViewFragment.QUIT_PAYMENT_PAGE, quitPaymentPage)
              webViewFragment.arguments = bundle
            }
            analyticsProvider.logEvent(Event.with(action = "paymentsStartHome"))
          }
        }
      }
    }
    return webViewFragment
  }

  private fun getPaymentOptionsUrl(
      transactionId: String,
      gatewayId: String = "1",
      flowType: String,
      tripId: String? = null,
      providerId: String? = null,
      productType: String? = null
  ): String =
      IxigoSDK.instance.getUrl(
          listOfNotNull(
                  "page" to "PAYMENT",
                  "gatewayId" to gatewayId,
                  "txnId" to transactionId,
                  "flowType" to flowType,
                  tripId?.let { "tripId" to it },
                  providerId?.let { "providerId" to it },
                  productType?.let { "productType" to it })
              .toMap())

  internal fun finishPayment(input: FinishPaymentInput): Boolean {
    with(input) {
      val callback = currentTransactions[transactionId]
      return if (callback != null) {
        if (success) {
          callback(Ok(ProcessPaymentResponse(nextUrl)))
        } else {
          callback(Err(ProcessPaymentProcessingError(nextUrl)))
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
      return instance
    }

    /** Boots up 3rd party sdk used by this sdk e.g. Juspay Native Sdk. */
    @JvmStatic
    fun bootUp(context: Context, juspayClientId: String) {
      JusPayGateway.preFetch(context, juspayClientId)
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

sealed class ProcessPaymentError

data class ProcessPaymentProcessingError(val nextUrl: String) : ProcessPaymentError()

data class ProcessPaymentNotLoginError(val error: Error) : ProcessPaymentError()
