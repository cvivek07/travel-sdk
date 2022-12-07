package com.ixigo.sdk.payment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.payment.data.PaymentFinished
import com.ixigo.sdk.payment.data.PhonePePaymentFinished

class PaymentViewModel : ViewModel() {

  val phonePeResultMutableLiveData: MutableLiveData<PhonePePaymentFinished> by lazy {
    MutableLiveData<PhonePePaymentFinished>()
  }

  fun setPhonePeResult(phonePePaymentFinished: PhonePePaymentFinished) {
    IxigoSDK.instance.analyticsProvider.logEvent(
        Event.with(action = "Phone Pe Payment Finished", label = phonePePaymentFinished.toString()))
    phonePeResultMutableLiveData.postValue(phonePePaymentFinished)
  }

  val gpayResultMutableLiveData: MutableLiveData<PaymentFinished> by lazy {
    MutableLiveData<PaymentFinished>()
  }

  fun setGpayPaymentResult(gpayPaymentFinished: PaymentFinished) {
    IxigoSDK.instance.analyticsProvider.logEvent(
        Event.with(action = "Gpay Payment Finished", label = gpayPaymentFinished.toString()))
    gpayResultMutableLiveData.postValue(gpayPaymentFinished)
  }

  val upiDirectResultMutableLiveData: MutableLiveData<PaymentFinished> by lazy {
    MutableLiveData<PaymentFinished>()
  }

  fun setUPIDirectPaymentResult(paymentFinished: PaymentFinished) {
    IxigoSDK.instance.analyticsProvider.logEvent(
        Event.with(action = "UPI Direct Payment Finished", label = paymentFinished.toString()))
    upiDirectResultMutableLiveData.postValue(paymentFinished)
  }
}
