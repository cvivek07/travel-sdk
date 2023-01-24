package com.ixigo.sdk.payment.minkasu_sdk

import android.webkit.WebView
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.payment.data.MinkasuInput
import com.ixigo.sdk.webview.WebViewFragment
import com.minkasu.android.twofa.model.Config
import com.minkasu.android.twofa.model.CustomerInfo
import com.minkasu.android.twofa.model.OrderInfo
import com.minkasu.android.twofa.sdk.Minkasu2faCallbackInfo
import com.minkasu.android.twofa.sdk.Minkasu2faSDK
import timber.log.Timber

class MinkasuSDKManager(
    private val webViewFragment: WebViewFragment,
    private val webView: WebView?
) {

  fun initMinkasu2FASDK(input: MinkasuInput) {
    try {
      webViewFragment.requireActivity().runOnUiThread {
        if (Minkasu2faSDK.isSupportedPlatform()) {
          val customer = CustomerInfo()
          with(customer) {
            firstName = input.firstName
            lastName = input.lastName
            email = input.email
            phone = input.phoneNumber // Format: +91XXXXXXXXXX (no spaces)
          }

          val config =
              Config.getInstance(input.merchantId, input.merchantToken, input.userId, customer)

          val orderInfo = OrderInfo().apply { orderId = input.transactionId }
          orderInfo.setBillingCategory(input.product)
          config.sdkMode = Config.PRODUCTION_MODE
          config.orderInfo = orderInfo
          webView?.let {
            Minkasu2faSDK.init(webViewFragment.requireActivity(), config, it) { callbackInfo ->
              val infoType = callbackInfo?.infoType
              val data = callbackInfo.getData()
              val properties = mutableMapOf<String, String>()
              properties["transactionId"] = input.transactionId
              when (infoType) {
                Minkasu2faCallbackInfo.INFO_TYPE_RESULT -> {
                  properties["reference_id"] = data.optString("reference_id")
                  properties["status"] = data.optString("status")
                  properties["source"] = data.optString("source")
                  properties["code"] = data.optInt("code").toString()
                }
                Minkasu2faCallbackInfo.INFO_TYPE_EVENT -> {
                  properties["reference_id"] = data.optString("reference_id")
                  properties["screen"] = data.optString("screen")
                  properties["event"] = data.optString("event")
                }
              }
              webViewFragment.analyticsProvider.logEvent(
                  Event(
                      "Minkasu SDK Payment Finished",
                      properties + mapOf("analyticsServiceName" to "CLEVERTAP"),
                      referrer = "https://www.ixigo.com"))
            }
          }
        }
      }
    } catch (t: Throwable) {
      Timber.d(t)
    }
  }
}
