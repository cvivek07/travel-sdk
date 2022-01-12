package com.ixigo.sdk.auth

import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result

typealias PartnerTokenResult = Result<PartnerToken>

typealias PartnerTokenCallback = (PartnerTokenResult) -> Unit

interface PartnerTokenProvider {
  fun fetchPartnerToken(callback: PartnerTokenCallback)
}

@Generated data class PartnerToken(val token: String)
