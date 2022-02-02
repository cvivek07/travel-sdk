package com.ixigo.sdk.auth

import android.app.Activity
import com.ixigo.sdk.common.Err

object EmptyPartnerTokenProvider : PartnerTokenProvider {
  override fun fetchPartnerToken(
      activity: Activity,
      requester: PartnerTokenProvider.Requester,
      callback: PartnerTokenCallback
  ) {
    callback(Err(PartnerTokenErrorServer()))
  }
}
