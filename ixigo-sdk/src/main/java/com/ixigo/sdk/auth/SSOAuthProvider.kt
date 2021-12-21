package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Generated
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
class SSOAuthProvider(private val partnerTokenProvider: PartnerTokenProvider) : AuthProvider {
  private val client: OkHttpClient by lazy { OkHttpClient() }
  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val responseJsonAdapter by lazy { moshi.adapter(RequestResponse::class.java) }

  override var authData: AuthData? = null
    private set

  override fun login(fragmentActivity: FragmentActivity, callback: AuthCallback): Boolean {
    val partnerToken = partnerTokenProvider.partnerToken ?: return false

    val formBody: FormBody = FormBody.Builder().add("authCode", partnerToken.token).build()

    val appInfo = IxigoSDK.getInstance().appInfo
    val config = IxigoSDK.getInstance().config
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
                val token = getAccessToken(response)
                if (token == null) {
                  callback(Err(Error("Could not get SSO Token")))
                } else {
                  val authData = AuthData(token)
                  this@SSOAuthProvider.authData = authData
                  callback(Ok(authData))
                }
              }

              private fun getAccessToken(response: Response): String? =
                  try {
                    responseJsonAdapter.fromJson(response.body!!.source())?.data?.accessToken
                  } catch (e: Exception) {
                    Timber.w(e, "Error trying to parse access_token")
                    null
                  }
            })
    return true
  }
}

private data class RequestResponse(val data: RequestResponseData)

private data class RequestResponseData(@Json(name = "access_token") val accessToken: String)

interface PartnerTokenProvider {
  val partnerToken: PartnerToken?
}

@Generated data class PartnerToken(val token: String)
