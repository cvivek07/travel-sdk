package com.ixigo.sdk.auth.test

import android.app.Activity
import androidx.fragment.app.FragmentActivity
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

  override var enabled: Boolean = true

  constructor(
      partnerId: String,
      partnerToken: PartnerToken?
  ) : this(
      PartnerTokenProvider.RequesterType.values()
          .map { Pair(PartnerTokenProvider.Requester(partnerId, it), partnerToken) }
          .toMap())
  constructor(
      partnerId: String,
      token: String?
  ) : this(partnerId, token?.let { PartnerToken((it)) } ?: null)

  override fun fetchPartnerToken(
      activity: FragmentActivity,
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
    fun forCustomer(partnerId: String, partnerToken: PartnerToken?) =
        FakePartnerTokenProvider(
            mapOf(
                PartnerTokenProvider.Requester(
                    partnerId, PartnerTokenProvider.RequesterType.CUSTOMER) to partnerToken))
  }
}
