package com.ixigo.sdk.remoteConfig

import kotlin.reflect.KClass

class FakeRemoteConfig : RemoteConfig {
  val values: MutableMap<String, Any> = mutableMapOf()
  override fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T =
      values[key] as? T ?: defaultValue
}

class FakeRemoteConfigProvider : RemoteConfigProvider {
  override val remoteConfig: FakeRemoteConfig = FakeRemoteConfig()
  val values
    get() = remoteConfig.values
}
