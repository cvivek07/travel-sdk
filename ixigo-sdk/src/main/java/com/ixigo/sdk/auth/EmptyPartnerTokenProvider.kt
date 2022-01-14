package com.ixigo.sdk.auth

import com.ixigo.sdk.common.Err

object EmptyPartnerTokenProvider : PartnerTokenProvider {
  override fun fetchPartnerToken(
      requester: PartnerTokenProvider.Requester,
      callback: PartnerTokenCallback
  ) {
    callback(Err(PartnerTokenErrorServer()))
  }
}
