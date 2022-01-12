package com.ixigo.sdk.webview

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentResult

class WebViewViewModel : ViewModel() {

  val paymentResult: MutableLiveData<NativePaymentResult> by lazy {
    MutableLiveData<NativePaymentResult>()
  }

  fun startNativePayment(activity: FragmentActivity, input: PaymentInput): Boolean {
    IxigoSDK.getInstance().apply {
      analyticsProvider.logEvent(Event.with(action = "paymentStart"))
      return paymentProvider.startPayment(activity, input) {
        IxigoSDK.getInstance()
            .analyticsProvider
            .logEvent(Event.with(action = "paymentFinished", label = it.simpleString()))
        paymentResult.postValue(NativePaymentResult(input, it))
      }
    }
  }
}

@Generated data class NativePaymentResult(val input: PaymentInput, val result: PaymentResult)
