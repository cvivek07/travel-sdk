package com.ixigo.sdk.auth

import com.ixigo.sdk.common.Err

object EmptyPartnerTokenProvider : PartnerTokenProvider {
  override fun fetchPartnerToken(callback: PartnerTokenCallback) {
    callback(Err(Error()))
  }
}
