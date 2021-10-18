package com.ixigo.sdk.app

import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.AuthResult

class FakeAuthProvider(val token: String?): AuthProvider {
    override fun login(callback: AuthCallback) {
        if (token == null) {
            callback(AuthResult.failure(Error("Not loggedIn")))
        } else {
            callback(AuthResult.success(AuthData(token)))
        }
    }
}