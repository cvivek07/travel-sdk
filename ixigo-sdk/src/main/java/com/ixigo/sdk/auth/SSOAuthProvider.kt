package com.ixigo.sdk.auth

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import okhttp3.*
import timber.log.Timber

/**
 * AuthProvider implementation that matches a partner token to an Ixigo token.
 *
 * The logic on how to match a partner token is kept server side. Some options:
 * - If a match on certain fields is found between the partner account and an ixigo account (email
 * or phone number match), this authProvider will return a token for the matched Ixigo account
 * - If there is no match, a new Ixigo Account might be created
 *
 * @property partnerTokenProvider
 */
class SSOAuthProvider(
    private val partnerTokenProvider: PartnerTokenProvider,
    private val appInfo: AppInfo = IxigoSDK.instance.appInfo
) : AuthProvider {
  private val client: OkHttpClient by lazy { OkHttpClient() }
  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val responseJsonAdapter by lazy { moshi.adapter(RequestResponse::class.java) }
  private val errorResponseJsonAdapter by lazy { moshi.adapter(ErrorResponse::class.java) }

  override var authData: AuthData? = null
    private set

  override fun login(
      fragmentActivity: FragmentActivity,
      partnerId: String,
      callback: AuthCallback
  ): Boolean {
    partnerTokenProvider.fetchPartnerToken(
        fragmentActivity,
        PartnerTokenProvider.Requester(partnerId, PartnerTokenProvider.RequesterType.CUSTOMER)) {
      when (it) {
        is Ok ->
            if (isIxigoApp()) {
              callback(Ok(AuthData(it.value.token)))
            } else {
              exchangeToken(it.value, callback)
            }
        is Err -> callback(Err(Error(it.value.message)))
      }
    }
    return true
  }

  private fun isIxigoApp(): Boolean {
    return (appInfo.clientId == "iximaad" || appInfo.clientId == "iximatr")
  }

  private fun exchangeToken(partnerToken: PartnerToken, callback: AuthCallback) {
    val formBody: FormBody = FormBody.Builder().add("authCode", partnerToken.token).build()

    val appInfo = IxigoSDK.instance.appInfo
    val config = IxigoSDK.instance.config
    val request =
        Request.Builder()
            .url(config.createUrl("api/v2/oauth/sso/login/token"))
            .addHeader("ixiSrc", appInfo.clientId)
            .addHeader("clientId", appInfo.clientId)
            .addHeader("apiKey", appInfo.apiKey)
            .addHeader("deviceId", appInfo.deviceId)
            .post(formBody)
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                Timber.e(e, "Error getting access token")
                callback(Err(Error(e)))
              }

              override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val token = getAccessToken(body)
                if (token == null) {
                  callback(Err(getError(body)))
                } else {
                  val authData = AuthData(token)
                  this@SSOAuthProvider.authData = authData
                  callback(Ok(authData))
                }
              }

              private fun getAccessToken(body: String?): String? =
                  try {
                    responseJsonAdapter.fromJson(body)?.data?.accessToken
                  } catch (e: Exception) {
                    Timber.w(e, "Error trying to parse access_token")
                    null
                  }

              private fun getError(body: String?): Error =
                  try {
                    val message = errorResponseJsonAdapter.fromJson(body)?.errors?.message
                    Error(message)
                  } catch (e: Exception) {
                    Timber.w(e, "Error trying to parse error message")
                    Error("Could not get SSO Token")
                  }
            })
  }
}

@Keep data class ErrorResponse(val errors: ErrorResponseErrors)

@Keep data class ErrorResponseErrors(val message: String)

@Keep data class RequestResponse(val data: RequestResponseData)

@Keep data class RequestResponseData(@Json(name = "access_token") val accessToken: String)
