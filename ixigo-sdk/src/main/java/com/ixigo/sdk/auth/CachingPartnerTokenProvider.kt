package com.ixigo.sdk.auth

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

internal class CachingPartnerTokenProvider(private val partnerTokenProvider: PartnerTokenProvider) :
    PartnerTokenProvider, ActivityResultHandler {
  var partnerToken: PartnerToken? = null
    private set

  override val enabled: Boolean
    get() = partnerTokenProvider.enabled

  override fun fetchPartnerToken(
      activity: FragmentActivity,
      requester: PartnerTokenProvider.Requester,
      callback: PartnerTokenCallback
  ) {
    partnerTokenProvider.fetchPartnerToken(activity, requester) {
      partnerToken =
          when (it) {
            is Err -> null
            is Ok -> it.value
          }
      callback(it)
    }
  }

  fun clear() {
    partnerToken = null
  }

  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (partnerTokenProvider is ActivityResultHandler) {
      partnerTokenProvider.handle(requestCode, resultCode, data)
      return true
    }
    return false
  }
}
