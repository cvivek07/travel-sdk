package com.ixigo.sdk.flights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.AuthResult

class WebViewViewModel: ViewModel() {
    val loginResult: MutableLiveData<LoginResult> by lazy {
        MutableLiveData<LoginResult>()
    }

    fun login(params: LoginParams) {
        IxigoSDK.getInstance().authProvider.login { authResult ->
            loginResult.value = LoginResult(params, authResult)
        }
    }
}

data class LoginParams(val successJSFunction: String, val failureJSFunction: String)

data class LoginResult(val loginParams: LoginParams, val result: AuthResult)