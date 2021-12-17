package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity

internal object EmptyAuthProvider: AuthProvider {
    override val authData: AuthData? = null

    override fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean = false
}