package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Err

object EmptyPartnerTokenProvider : PartnerTokenProvider {
  override fun fetchPartnerToken(
      activity: FragmentActivity,
      requester: PartnerTokenProvider.Requester,
      callback: PartnerTokenCallback
  ) {
    callback(Err(PartnerTokenErrorServer()))
  }
}
