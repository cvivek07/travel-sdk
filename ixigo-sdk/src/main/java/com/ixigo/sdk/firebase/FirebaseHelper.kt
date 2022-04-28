package com.ixigo.sdk.firebase

import android.content.Context
import com.google.firebase.FirebaseOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.common.NoCoverage
import com.ixigo.sdk.remoteConfig.RemoteConfig
import com.ixigo.sdk.remoteConfig.RemoteConfigFirebase
import com.ixigo.sdk.remoteConfig.RemoteConfigProvider
import com.ixigo.sdk.remoteConfig.get
import timber.log.Timber

private const val baseFirebaseAppId = "1:132902544575:ios:f0d90487b4debd2971120a"
private const val baseFirebaseAppName = "ixigo-sdk-base"
private const val firebaseAppName = "ixigo-sdk"

@NoCoverage
class FirebaseHelper(private val context: Context, appInfo: AppInfo) : RemoteConfigProvider {

  init {
    initFirebaseApp(baseFirebaseAppName, baseFirebaseAppId) {
      val applicationIdMap =
          RemoteConfigFirebase(it)["clientId_to_firebaseAppId", defaultApplicationIdMap]
      val applicationId = applicationIdMap[appInfo.clientId]
      if (applicationId != null) {
        initFirebaseApp(firebaseAppName, applicationId)
      }
    }
  }

  private val defaultApplicationIdMap =
      mapOf(
          "abhibus" to "1:132902544575:android:9270763a13b544f571120a",
          "confirmtckt" to "1:132902544575:android:956347285b51bc9771120a",
          "iximatr" to "1:132902544575:android:6a2d77a68dc54bb371120a",
          "iximaad" to "1:132902544575:android:4d24ef043c99312071120a")

  override val remoteConfig: RemoteConfig by lazy {
    val firebaseApp =
        if (kotlin.runCatching { Firebase.app(firebaseAppName) }.getOrNull() == null) {
          Firebase.app(baseFirebaseAppName)
        } else {
          Firebase.app(firebaseAppName)
        }
    RemoteConfigFirebase(Firebase.remoteConfig(firebaseApp))
  }

  private fun initFirebaseApp(
      name: String,
      applicationId: String,
      onRemoteConfigReady: ((FirebaseRemoteConfig) -> Unit)? = null
  ) {
    val options =
        FirebaseOptions.Builder()
            .setProjectId("ixigo-sdk-demo-app")
            .setApplicationId(applicationId)
            .setApiKey("AIzaSyBEJrf3SjSFMUBahV5eL20pquCW6auVSfA")
            .build()

    if (kotlin.runCatching { Firebase.app(name) }.getOrNull() == null) {
      Firebase.initialize(context, options, name)
    }
    val firebaseApp = Firebase.app(name)

    val firebaseRemoteConfig = Firebase.remoteConfig(firebaseApp)
    val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
    firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
    firebaseRemoteConfig.fetchAndActivate().addOnSuccessListener {
      Timber.d("Successfully updated remote config for $name")
      onRemoteConfigReady?.invoke(firebaseRemoteConfig)
    }
    firebaseRemoteConfig.fetchAndActivate().addOnFailureListener {
      Timber.e(it, "Error updating remote config for $name")
      onRemoteConfigReady?.invoke(firebaseRemoteConfig)
    }
  }
}
