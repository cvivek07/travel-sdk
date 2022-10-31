package com.ixigo.sdk.payment.minkasu_sdk

import com.ixigo.sdk.payment.data.MinkasuInput
import com.ixigo.sdk.webview.WebViewFragment
import com.minkasu.android.twofa.model.Config
import com.minkasu.android.twofa.model.CustomerInfo
import com.minkasu.android.twofa.model.OrderInfo
import com.minkasu.android.twofa.sdk.Minkasu2faSDK
import timber.log.Timber

class MinkasuSDKManager(private val webViewFragment: WebViewFragment) {

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
          config.sdkMode = Config.PRODUCTION_MODE
          config.orderInfo = orderInfo
          Minkasu2faSDK.init(webViewFragment.requireActivity(), config, webViewFragment.webView)
        }
      }
    } catch (e: java.lang.Exception) {
      Timber.tag("Minkasu Exception").e(e.toString())
    }
  }
}
