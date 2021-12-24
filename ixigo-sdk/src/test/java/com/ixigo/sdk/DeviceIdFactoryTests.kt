package com.ixigo.sdk

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeviceIdFactoryTests {

  @Mock lateinit var mockContext: Context
  @Mock lateinit var mockContentResolver: ContentResolver

  @Before
  fun setup() {
    Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)
  }

  @Test
  fun `test UUID is generated randomly and saved to preferences`() {
    val secureId = "secureId"
    Mockito.mockStatic(Settings.Secure::class.java).use { mockedSecure ->
      mockedSecure
          .`when`<Any> {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ANDROID_ID)
          }
          .thenReturn(secureId)
      val deviceId = DeviceIdFactory(mockContext).deviceID
      assertEquals(secureId, deviceId)
    }
  }
}
