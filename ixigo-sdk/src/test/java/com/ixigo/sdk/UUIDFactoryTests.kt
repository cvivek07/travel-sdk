package com.ixigo.sdk

import android.content.Context
import android.content.SharedPreferences
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UUIDFactoryTests {

  @Mock lateinit var mockContext: Context
  @Mock lateinit var mockPreferences: SharedPreferences
  @Mock lateinit var mockEditor: SharedPreferences.Editor

  @Before
  fun setup() {
    Mockito.`when`(mockContext.getSharedPreferences("uuidfactory.xml", Context.MODE_PRIVATE))
        .thenReturn(mockPreferences)
    Mockito.`when`(mockPreferences.edit()).thenReturn(mockEditor)
  }

  @Test
  fun `test UUID is generated randomly and saved to preferences`() {
    val randomUUID = UUID.randomUUID()
    Mockito.mockStatic(UUID::class.java).use { mockedUUID ->
      mockedUUID.`when`<Any> { UUID.randomUUID() }.thenReturn(randomUUID)
      val uuid = UUIDFactory(mockContext).uuid
      assertEquals(randomUUID, uuid)
      verify(mockEditor).putString("uuid", randomUUID.toString())
      verify(mockEditor).apply()
    }
  }

  @Test
  fun `test UUID is read from preferences if it already exists`() {
    val savedUUID = UUID.randomUUID()
    Mockito.`when`(mockPreferences.getString("uuid", null)).thenReturn(savedUUID.toString())

    val uuid = UUIDFactory(mockContext).uuid
    assertEquals(savedUUID, uuid)
  }
}
