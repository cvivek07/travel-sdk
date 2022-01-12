package com.ixigo.sdk.auth.test

import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenCallback
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

class FakePartnerTokenProvider(val partnerToken: PartnerToken?) : PartnerTokenProvider {

  constructor(token: String?) : this(token?.let { PartnerToken((it)) } ?: null)

  override fun fetchPartnerToken(callback: PartnerTokenCallback) {
    if (partnerToken == null) {
      callback(Err(Error()))
    } else {
      callback(Ok(partnerToken))
    }
  }
}
