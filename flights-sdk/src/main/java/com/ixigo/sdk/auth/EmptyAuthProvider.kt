package com.ixigo.sdk.auth

internal object EmptyAuthProvider: AuthProvider {
    override val authData: AuthData? = null

    override fun login(callback: AuthCallback): Boolean = false
}