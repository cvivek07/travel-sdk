package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.common.Generated
import com.ixigo.sdk.common.Result

typealias PartnerTokenResult = Result<PartnerToken, PartnerTokenError>

typealias PartnerTokenCallback = (PartnerTokenResult) -> Unit

/** Interface to delegate to a Host App the creation/retrieval of a Host App token */
interface PartnerTokenProvider {

  /**
   * Implementations of this method should create/retrieve a Host App auth token
   *
   * @param activity
   * @param requester type of requester
   * @param callback
   */
  fun fetchPartnerToken(
      activity: FragmentActivity,
      requester: Requester,
      callback: PartnerTokenCallback
  )

  /**
   * Whether this provider is enabled or not. If not enabled, it will not be considered for auth
   * purposes
   */
  val enabled: Boolean
    get() = true

  data class Requester(val partnerId: String, val type: RequesterType)

  /** Identifies who is requesting a partnerToken */
  enum class RequesterType {
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
