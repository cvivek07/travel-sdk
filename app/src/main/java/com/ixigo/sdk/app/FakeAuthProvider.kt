package com.ixigo.sdk.app

import android.os.Handler
import com.ixigo.sdk.auth.AuthCallback
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.AuthResult

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