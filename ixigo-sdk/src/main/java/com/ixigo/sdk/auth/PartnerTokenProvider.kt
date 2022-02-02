package com.ixigo.sdk.auth

import android.app.Activity
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result

typealias PartnerTokenResult = Result<PartnerToken, PartnerTokenError>

typealias PartnerTokenCallback = (PartnerTokenResult) -> Unit

interface PartnerTokenProvider {
  fun fetchPartnerToken(activity: Activity, requester: Requester, callback: PartnerTokenCallback)

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

sealed class PartnerTokenError(val code: Int, val message: String)

class PartnerTokenErrorUserNotLoggedIn(message: String = "User not logged into the app") :
    PartnerTokenError(101, message)

class PartnerTokenErrorUserDeniedLogin(
    message: String = "User denied to grant access of login details"
) : PartnerTokenError(102, message)

class PartnerTokenErrorServer(message: String = "Server Error") : PartnerTokenError(103, message)

class PartnerTokenErrorSDK(message: String = "SDK Error") : PartnerTokenError(104, message)
