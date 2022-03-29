package com.ixigo.sdk.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.reflect.KClass
import timber.log.Timber

class RemoteConfigProvider(private val remoteConfig: FirebaseRemoteConfig) {
  fun <T : Any> getValue(
      key: String,
      defaultValue: T,
      deserializer: RemoteConfigDeserializer<T>
  ): T {
    return when (defaultValue) {
      is String -> remoteConfig.getString(key).takeIf { it != DEFAULT_VALUE_FOR_STRING } as? T
      is Long -> remoteConfig.getLong(key).takeIf { it != DEFAULT_VALUE_FOR_LONG } as? T
      is Double -> remoteConfig.getDouble(key).takeIf { it != DEFAULT_VALUE_FOR_DOUBLE } as? T
      is Boolean -> remoteConfig.getBoolean(key).takeIf { it != DEFAULT_VALUE_FOR_BOOLEAN } as? T
      else -> deserializer.deserialize(remoteConfig.getString(key))
    }
        ?: defaultValue
  }

  inline fun <reified T : Any> get(key: String, defaultValue: T): T =
      getValue(key, defaultValue, MoshiDeserializer(T::class))
}

class MoshiDeserializer<T : Any>(private val clazz: KClass<T>) : RemoteConfigDeserializer<T> {
  private val adapter by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(clazz.java)
  }

  override fun deserialize(stringValue: String): T? {
    return try {
      adapter.fromJson(stringValue)
    } catch (e: Exception) {
      Timber.e(e, "Error deserializing json=$stringValue")
      null
    }
  }
}

interface RemoteConfigDeserializer<T> {
  fun deserialize(stringValue: String): T?
}
