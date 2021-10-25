package com.ixigo.sdk.auth.test

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

class FakeAuthProvider(val token: String?, override val authData: AuthData? = null):
    AuthProvider {

    override fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean {
        return if (token == null) {
            callback(Err(Error()))
            true
        } else {
            callback(Ok(AuthData(token)))
            true
        }
    }
}