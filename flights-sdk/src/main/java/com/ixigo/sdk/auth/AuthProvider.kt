package com.ixigo.sdk.auth

typealias AuthResult = Result<AuthData>
typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
    fun login(callback: AuthCallback)
}