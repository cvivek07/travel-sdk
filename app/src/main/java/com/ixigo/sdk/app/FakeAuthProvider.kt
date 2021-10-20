package com.ixigo.sdk.app

import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.AuthResult

class FakeAuthProvider(private val loginToken: String?, override val authData: AuthData? = null): AuthProvider {

    override fun login(callback: AuthCallback) {
        if (loginToken == null) {
            callback(AuthResult.failure(Error("Not loggedIn")))
        } else {
            callback(AuthResult.success(AuthData(loginToken)))
        }
    }
}