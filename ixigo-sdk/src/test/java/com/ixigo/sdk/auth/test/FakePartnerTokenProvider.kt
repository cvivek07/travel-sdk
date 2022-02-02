package com.ixigo.sdk.auth.test

import android.app.Activity
import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenCallback
import com.ixigo.sdk.auth.PartnerTokenErrorSDK
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok

interface ActivityResultPartnerTokenProvider : PartnerTokenProvider, ActivityResultHandler

class FakePartnerTokenProvider(
    var partnerTokenMap: Map<PartnerTokenProvider.Requester, PartnerToken?> = mapOf(),
) : PartnerTokenProvider {

  var passedActivity: Activity? = null

  constructor(
      partnerToken: PartnerToken?
  ) : this(PartnerTokenProvider.Requester.values().map { Pair(it, partnerToken) }.toMap())
  constructor(token: String?) : this(token?.let { PartnerToken((it)) } ?: null)

  override fun fetchPartnerToken(
      activity: Activity,
      requester: PartnerTokenProvider.Requester,
      callback: PartnerTokenCallback
  ) {
    passedActivity = activity
    val partnerToken = partnerTokenMap[requester]
    if (partnerToken == null) {
      callback(Err(PartnerTokenErrorSDK()))
    } else {
      callback(Ok(partnerToken))
    }
  }

  companion object {
    fun forCustomer(partnerToken: PartnerToken?) =
        FakePartnerTokenProvider(mapOf(PartnerTokenProvider.Requester.CUSTOMER to partnerToken))
  }
}
