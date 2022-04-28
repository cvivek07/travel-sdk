package com.ixigo.sdk.remoteConfig

import kotlin.reflect.KClass

interface RemoteConfig {
  fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T
}

inline operator fun <reified T : Any> RemoteConfig.get(key: String, defaultValue: T) =
    get(key, defaultValue, T::class)

interface RemoteConfigProvider {
  val remoteConfig: RemoteConfig
}
