package com.ixigo.sdk.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.reflect.KClass
import timber.log.Timber

class FirebaseRemoteConfigProvider(private val remoteConfig: FirebaseRemoteConfig) :
    RemoteConfigProvider {

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

  override fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T {
    return when (defaultValue) {
      is String -> remoteConfig.getString(key).takeIf { it != DEFAULT_VALUE_FOR_STRING } as? T
      is Long -> remoteConfig.getLong(key).takeIf { it != DEFAULT_VALUE_FOR_LONG } as? T
      is Double -> remoteConfig.getDouble(key).takeIf { it != DEFAULT_VALUE_FOR_DOUBLE } as? T
      is Boolean -> remoteConfig.getBoolean(key).takeIf { it != DEFAULT_VALUE_FOR_BOOLEAN } as? T
      else -> {
        val stringValue = remoteConfig.getString(key)
        try {
          moshi.adapter(clazz.java).fromJson(stringValue) as? T
        } catch (e: Exception) {
          Timber.e(e, "Error deserializing json=$stringValue")
          null
        }
      }
    }
        ?: defaultValue
  }
}
