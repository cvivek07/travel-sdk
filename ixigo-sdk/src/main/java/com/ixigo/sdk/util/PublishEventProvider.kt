package com.ixigo.sdk.util

import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.remoteConfig.PublishEventRemoteConfig
import com.ixigo.sdk.remoteConfig.get
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

class PublishEventProvider : AnalyticsProvider {

  override fun logEvent(event: Event) {
    val publishEventRemoteConfig: PublishEventRemoteConfig =
        IxigoSDK.instance.remoteConfigProvider.remoteConfig[
            "publishEvent", PublishEventRemoteConfig(enabled = false)]

    if (publishEventRemoteConfig.enabled) {
      val appInfo = IxigoSDK.instance.appInfo
      val config = IxigoSDK.instance.config
      val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
      val body = event.properties["request"]?.toRequestBody(mediaType)

      val request =
          body?.let {
            Request.Builder()
                .url(config.createUrl("payments/v4/publish-events"))
                .addHeader("ixiSrc", appInfo.clientId)
                .addHeader("clientId", appInfo.clientId)
                .addHeader("appVersion", appInfo.appVersionString)
                .addHeader("apiKey", appInfo.apiKey)
                .addHeader("deviceId", appInfo.deviceId)
                .addHeader("authorization", "Bearer ${event.properties["Authorization"]}")
                .addHeader("deviceName", android.os.Build.MODEL)
                .post(it)
                .build()
          }

      request?.let {
        OkHttpClient()
            .newCall(request)
            .enqueue(
                object : Callback {
                  override fun onFailure(call: Call, e: IOException) {
                    Timber.e("publish event: failed")
                  }

                  override fun onResponse(call: Call, response: Response) {
                    Timber.d("publish event: $body")
                  }
                })
      }
    }
  }
}
