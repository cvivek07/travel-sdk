package com.ixigo.sdk.auth
import com.github.michaelbull.result.Result

typealias AuthResult = Result<AuthData, Error>
typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
    val authData: AuthData?
    fun login(callback: AuthCallback): Boolean
}