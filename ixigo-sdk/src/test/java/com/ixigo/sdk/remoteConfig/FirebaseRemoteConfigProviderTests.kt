package com.ixigo.sdk.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class FirebaseRemoteConfigProviderTests {

  @Mock lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

  @Test
  fun `test Json value`() {
    whenever(firebaseRemoteConfig.getString("known"))
        .thenReturn("""{"name": "johnnie", "age": 25}""")
    whenever(firebaseRemoteConfig.getString("unknown")).thenReturn(DEFAULT_VALUE_FOR_STRING)

    val defaultPerson = Person("default", 30)
    val provider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    val person = provider["known", defaultPerson]
    assertEquals(Person("johnnie", 25), person)

    assertEquals(defaultPerson, provider["unknown", defaultPerson])
  }

  @Test
  fun `test string value`() {
    whenever(firebaseRemoteConfig.getString("known")).thenReturn("value")
    whenever(firebaseRemoteConfig.getString("unknown")).thenReturn(DEFAULT_VALUE_FOR_STRING)

    val defaultValue = "defaultValue"
    val provider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    val value = provider["known", defaultValue]
    assertEquals("value", value)
    assertEquals(defaultValue, provider.get("unknown", defaultValue))
  }

  @Test
  fun `test long value`() {
    whenever(firebaseRemoteConfig.getLong("known")).thenReturn(10)
    whenever(firebaseRemoteConfig.getLong("unknown")).thenReturn(DEFAULT_VALUE_FOR_LONG)

    val defaultValue: Long = 15
    val provider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    val value = provider["known", defaultValue]
    assertEquals(10, value)
    assertEquals(defaultValue, provider.get("unknown", defaultValue))
  }

  @Test
  fun `test double value`() {
    whenever(firebaseRemoteConfig.getDouble("known")).thenReturn(10.0)
    whenever(firebaseRemoteConfig.getDouble("unknown")).thenReturn(DEFAULT_VALUE_FOR_DOUBLE)

    val defaultValue: Double = 15.0
    val provider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    val value = provider["known", defaultValue]
    assertEquals(10.0, value, 0.01)
    assertEquals(defaultValue, provider.get("unknown", defaultValue), 0.01)
  }

  @Test
  fun `test boolean value`() {
    whenever(firebaseRemoteConfig.getBoolean("known")).thenReturn(true)
    whenever(firebaseRemoteConfig.getBoolean("unknown")).thenReturn(DEFAULT_VALUE_FOR_BOOLEAN)

    val defaultValue = false
    val provider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    val value = provider["known", defaultValue]
    assertEquals(true, value)
    assertEquals(defaultValue, provider.get("unknown", defaultValue))
  }
}

data class Person(val name: String, val age: Int)
