package com.ixigo.sdk.webview

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.AuthResult
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentResult

class WebViewViewModel : ViewModel() {
  val loginResult: MutableLiveData<LoginResult> by lazy { MutableLiveData<LoginResult>() }

  val paymentResult: MutableLiveData<NativePaymentResult> by lazy {
    MutableLiveData<NativePaymentResult>()
  }

  fun login(fragmentActivity: FragmentActivity, params: LoginParams): Boolean {
    IxigoSDK.getInstance().apply {
      analyticsProvider.logEvent(Event.with(action = "loginStart"))
      return IxigoSDK.getInstance().authProvider.login(fragmentActivity) {
        analyticsProvider.logEvent(Event.with(action = "loginFinished", label = it.simpleString()))
        loginResult.postValue(LoginResult(params, it))
      }
    }
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

data class LoginParams(val successJSFunction: String, val failureJSFunction: String)

data class LoginResult(val loginParams: LoginParams, val result: AuthResult)

@Generated data class NativePaymentResult(val input: PaymentInput, val result: PaymentResult)
