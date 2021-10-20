package com.ixigo.sdk.auth

typealias AuthResult = Result<AuthData>
typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
    val authData: AuthData?
    fun login(callback: AuthCallback)
}