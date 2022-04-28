package com.ixigo.sdk.payment

import android.content.Intent
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.remoteConfig.RemoteConfigProvider
import com.ixigo.sdk.remoteConfig.get

class DefaultPaymentProvider(
    private val remoteConfigProvider: RemoteConfigProvider,
    private val customPaymentProvider: PaymentProvider? = null,
    modePaymentProvider: (PaymentMode) -> PaymentProvider = ::defaultModePaymentProvider
) : PaymentProvider, ActivityResultHandler {

  private val paymentProvider: PaymentProvider by lazy {
    val paymentConfig =
        remoteConfigProvider.remoteConfig[
            "payment", PaymentRemoteConfig(mode = PaymentMode.SDK, allowHostAppPayment = true)]
    if (paymentConfig.allowHostAppPayment && customPaymentProvider != null) {
      customPaymentProvider
    } else {
      modePaymentProvider(paymentConfig.mode)
    }
  }
  override fun startPayment(
      activity: FragmentActivity,
      input: PaymentInput,
      callback: PaymentCallback
  ): Boolean {
    return paymentProvider.startPayment(activity, input, callback)
  }

  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    return if (paymentProvider is ActivityResultHandler) {
      (paymentProvider as ActivityResultHandler).handle(requestCode, resultCode, data)
    } else {
      false
    }
  }
}

private fun defaultModePaymentProvider(mode: PaymentMode): PaymentProvider =
    when (mode) {
      PaymentMode.SDK -> PaymentSDKPaymentProvider()
      PaymentMode.WEB -> DisabledPaymentProvider
    }

@Keep
enum class PaymentMode {
  SDK,
  WEB
}

internal data class PaymentRemoteConfig(val mode: PaymentMode, val allowHostAppPayment: Boolean)
