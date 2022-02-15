package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Result

typealias AuthResult = Result<AuthData, Error>

typealias AuthCallback = (AuthResult) -> Unit

interface AuthProvider {
  val authData: AuthData?
  fun login(fragmentActivity: FragmentActivity, partnerId: String, callback: AuthCallback): Boolean
}
