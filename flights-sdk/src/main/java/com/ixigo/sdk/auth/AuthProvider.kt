package com.ixigo.sdk.auth

import com.ixigo.sdk.common.Result

typealias AuthResult = Result<AuthData>
typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
    val authData: AuthData?
    fun login(callback: AuthCallback): Boolean
}