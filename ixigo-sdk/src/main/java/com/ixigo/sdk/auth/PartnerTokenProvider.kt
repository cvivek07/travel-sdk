package com.ixigo.sdk.auth

import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result

typealias PartnerTokenResult = Result<PartnerToken>

typealias PartnerTokenCallback = (PartnerTokenResult) -> Unit

interface PartnerTokenProvider {
  fun fetchPartnerToken(requester: Requester, callback: PartnerTokenCallback)

  /** Identifies who is requesting a partnerToken */
  enum class Requester {
    /**
     * Customer initiated this request. You are allow to show UI in order to get the partner token
     * if needed eg: display a login screen if the customer is not logged in.
     */
    CUSTOMER,

    /** SDK initiated the request. This is generally used to prefetch a token */
    SDK
  }
}

@Generated data class PartnerToken(val token: String)
