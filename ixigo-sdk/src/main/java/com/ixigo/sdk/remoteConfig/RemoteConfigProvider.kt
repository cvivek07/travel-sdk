package com.ixigo.sdk.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig.*
import kotlin.reflect.KClass

interface RemoteConfigProvider {
  fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T
}

inline operator fun <reified T : Any> RemoteConfigProvider.get(key: String, defaultValue: T) =
    get(key, defaultValue, T::class)
