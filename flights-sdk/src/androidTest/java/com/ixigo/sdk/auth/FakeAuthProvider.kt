package com.ixigo.sdk.auth

import android.os.Handler
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

class FakeAuthProvider(private val loginToken: String?, override val authData: AuthData? = null): AuthProvider {

    override fun login(callback: AuthCallback): Boolean {
        Handler().post{
            if (loginToken == null) {
                callback(Err(Error("Not loggedIn")))
            } else {
                callback(Ok(AuthData(loginToken)))
            }
        }
        return true
    }
}