package com.ixigo.sdk.payment.phonepe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.payment.data.PhonePePaymentFinished

class PhonePeViewModel : ViewModel() {

  val phonePeResultMutableLiveData: MutableLiveData<PhonePePaymentFinished> by lazy {
    MutableLiveData<PhonePePaymentFinished>()
  }

  fun setPhonePeResult(phonePePaymentFinished: PhonePePaymentFinished) {
    IxigoSDK.instance.analyticsProvider.logEvent(
        Event.with(action = "Phone Pe Payment Finished", label = phonePePaymentFinished.toString()))
    phonePeResultMutableLiveData.postValue(phonePePaymentFinished)
  }
}
