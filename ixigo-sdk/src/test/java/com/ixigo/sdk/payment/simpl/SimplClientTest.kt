package com.ixigo.sdk.payment.simpl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simpl.android.fingerprint.SimplFingerprint
import com.simpl.android.fingerprint.SimplFingerprintListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SimplClientTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val mockFactory = mock<SimplFingerPrintFactory>()
  private val mockSimplFingerprint = mock<SimplFingerprint>()
  private val simplClient = SimplClient(context, factory = mockFactory)

  @Before
  fun setup() {
    Mockito.`when`(mockFactory.create(any(), any())).thenReturn(mockSimplFingerprint)
  }

  @Test
  fun `test simplClient uses sdk class to get fingerprint`() = runTest {
    val mobile = "9876543210"
    val email = "test@gmail.com"
    val dummyFingerprint = "test fingerprint"

    val callbackCaptor = argumentCaptor<SimplFingerprintListener>()
    Mockito.`when`(mockSimplFingerprint.generateFingerprint(callbackCaptor.capture())).thenAnswer {
      val callback = callbackCaptor.lastValue
      callback.fingerprintData(dummyFingerprint)
      return@thenAnswer it
    }

    val actualFingerprint = simplClient.getFingerPrint(mobile, email)
    Mockito.verify(mockSimplFingerprint).generateFingerprint(any())
    assertEquals(dummyFingerprint, actualFingerprint)
  }
}
