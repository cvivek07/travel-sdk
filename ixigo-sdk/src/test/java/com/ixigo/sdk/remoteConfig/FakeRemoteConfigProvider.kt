package com.ixigo.sdk.remoteConfig

import kotlin.reflect.KClass

class FakeRemoteConfigProvider : RemoteConfigProvider {
  val values: MutableMap<String, Any> = mutableMapOf()
  override fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T =
      values[key] as? T ?: defaultValue
}
