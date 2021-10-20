package com.ixigo.sdk.auth

import android.os.Handler

class FakeAuthProvider(private val loginToken: String?, override val authData: AuthData? = null): AuthProvider {

    override fun login(callback: AuthCallback): Boolean {
        Handler().post{
            if (loginToken == null) {
                callback(AuthResult.failure(Error("Not loggedIn")))
            } else {
                callback(AuthResult.success(AuthData(loginToken)))
            }
        }
        return true
    }
}