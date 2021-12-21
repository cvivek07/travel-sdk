package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Result

typealias AuthResult = Result<AuthData>

typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
  val authData: AuthData?
  fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean
}
