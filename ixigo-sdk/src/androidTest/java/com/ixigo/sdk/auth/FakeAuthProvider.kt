package com.ixigo.sdk.auth

import android.os.Handler
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

class FakeAuthProvider(private val loginToken: String?, override val authData: AuthData? = null) :
    AuthProvider {

  override fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean {
    Handler().post {
      if (loginToken == null) {
        callback(Err(Error("Not loggedIn")))
      } else {
        callback(Ok(AuthData(loginToken)))
      }
    }
    return true
  }
}
