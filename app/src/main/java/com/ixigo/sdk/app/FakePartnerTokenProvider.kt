package com.ixigo.sdk.app

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenCallback
import com.ixigo.sdk.auth.PartnerTokenErrorUserNotLoggedIn
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

private const val LoginRequestCode = 1001

class FakePartnerTokenProvider(private val token: String): PartnerTokenProvider, ActivityResultHandler {
  private var partnerToken: PartnerToken? = null
  private var callback: PartnerTokenCallback? = null

  override fun fetchPartnerToken(
    activity: FragmentActivity,
    requester: PartnerTokenProvider.Requester,
    callback: PartnerTokenCallback
  ) {
    if (partnerToken != null) {
      partnerToken?.let { callback(Ok(it))}
    } else {
      this.callback = callback
      val intent = Intent(activity, FakeLoginActivity::class.java)
      intent.putExtra("token", token)
      activity.startActivityForResult(intent, LoginRequestCode)
    }
  }

  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (requestCode == LoginRequestCode) {
      val token = data?.getStringExtra("token")
      if (token != null) {
        partnerToken = PartnerToken(token)
        callback?.invoke(Ok(partnerToken!!))
      } else {
        callback?.invoke(Err(PartnerTokenErrorUserNotLoggedIn()))
      }
      return true
    }
    return false
  }
}