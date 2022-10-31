package com.ixigo.sdk.payment.gpay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.payment.data.GpayPaymentFinished

class GpayViewModel : ViewModel() {

  val gpayResultMutableLiveData: MutableLiveData<GpayPaymentFinished> by lazy {
    MutableLiveData<GpayPaymentFinished>()
  }

  fun setGpayPaymentResult(gpayPaymentFinished: GpayPaymentFinished) {
    IxigoSDK.instance.analyticsProvider.logEvent(
        Event.with(action = "Gpay Payment Finished", label = gpayPaymentFinished.toString()))
    gpayResultMutableLiveData.postValue(gpayPaymentFinished)
  }
}
