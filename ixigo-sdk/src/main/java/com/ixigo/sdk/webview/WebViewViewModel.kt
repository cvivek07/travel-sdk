package com.ixigo.sdk.webview

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.NoCoverage
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentInternalError
import com.ixigo.sdk.payment.PaymentResult

class WebViewViewModel constructor(private val ixigoSDK: IxigoSDK) : ViewModel() {

  val paymentResult: MutableLiveData<NativePaymentResult> by lazy {
    MutableLiveData<NativePaymentResult>()
  }

  fun startNativePayment(activity: FragmentActivity, input: PaymentInput): Boolean {
    with(ixigoSDK) {
      analyticsProvider.logEvent(Event.with(action = "paymentStart"))
      return paymentProvider.startPayment(activity, input) {
        analyticsProvider.logEvent(
            Event.with(action = "paymentFinished", label = it.simpleString()))
        paymentResult.postValue(NativePaymentResult(input, it))
      }
    }
  }

  fun startNativePaymentAsync(
      activity: FragmentActivity,
      input: PaymentInput
  ): LiveData<NativePaymentResult> {
    val paymentResultLiveData = MutableLiveData<NativePaymentResult>()
    with(ixigoSDK) {
      analyticsProvider.logEvent(Event.with(action = "paymentStart"))
      val paymentInitiated =
          paymentProvider.startPayment(activity, input) {
            analyticsProvider.logEvent(
                Event.with(action = "paymentFinished", label = it.simpleString()))
            paymentResultLiveData.postValue(NativePaymentResult(input, it))
          }

      if (paymentInitiated.not()) {
        paymentResultLiveData.postValue(
            NativePaymentResult(input, Err(PaymentInternalError("Incorrect input"))))
      }
    }
    return paymentResultLiveData
  }

  class Factory constructor(private val ixigoSdk: IxigoSDK) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return WebViewViewModel(ixigoSdk) as T
    }
  }
}

@NoCoverage data class NativePaymentResult(val input: PaymentInput, val result: PaymentResult)
