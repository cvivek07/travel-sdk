package com.ixigo.sdk.flights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.AuthResult
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentResult

class WebViewViewModel: ViewModel() {
    val loginResult: MutableLiveData<LoginResult> by lazy {
        MutableLiveData<LoginResult>()
    }

    val paymentResult: MutableLiveData<NativePaymentResult> by lazy {
        MutableLiveData<NativePaymentResult>()
    }

    fun login(params: LoginParams) =
        IxigoSDK.getInstance().authProvider.login {
            loginResult.postValue(LoginResult(params, it))
        }

    fun startNativePayment(input: PaymentInput): Boolean =
        IxigoSDK.getInstance().paymentProvider.startPayment(input) {
            paymentResult.postValue(NativePaymentResult(input, it))
        }
}

data class LoginParams(val successJSFunction: String, val failureJSFunction: String)

data class LoginResult(val loginParams: LoginParams, val result: AuthResult)

data class NativePaymentResult(val input: PaymentInput, val result: PaymentResult)