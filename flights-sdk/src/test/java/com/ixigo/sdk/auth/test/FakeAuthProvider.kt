package com.ixigo.sdk.auth.test

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.AuthProvider

class FakeAuthProvider(val token: String?, override val authData: AuthData? = null):
    AuthProvider {

    override fun login(callback: AuthCallback): Boolean {
        return if (token == null) {
            callback(Err(Error()))
            true
        } else {
            callback(Ok(AuthData(token)))
            true
        }
    }
}