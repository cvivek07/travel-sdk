package com.ixigo.sdk.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.common.api.Status
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class OtpSmsRetrieverTest {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockSmsRetrieverClient: SmsRetrieverClient

  @Mock private lateinit var mockActivity: Activity
  private lateinit var otpSmsRetriever: OtpSmsRetriever

  @Captor lateinit var broadcastReceiverCaptor: ArgumentCaptor<BroadcastReceiver>
  @Captor lateinit var requestCodeCaptor: ArgumentCaptor<Int>

  @Before
  fun setup() {
    otpSmsRetriever = OtpSmsRetriever(mockActivity, mockSmsRetrieverClient)
  }

  @Test
  fun `test successful sms retrieval`() {
    var smsResult: OtpSmsRetrieverResult? = null
    otpSmsRetriever.startListening { smsResult = it }
    verify(mockActivity)
        .registerReceiver(
            capture(broadcastReceiverCaptor), any(), eq(SmsRetriever.SEND_PERMISSION), eq(null))
    verify(mockSmsRetrieverClient).startSmsUserConsent(null)

    val extraConsentIntent = Intent()
    val intent =
        Intent().apply {
          action = SmsRetriever.SMS_RETRIEVED_ACTION
          putExtra(SmsRetriever.EXTRA_STATUS, Status.RESULT_SUCCESS)
          putExtra(SmsRetriever.EXTRA_CONSENT_INTENT, extraConsentIntent)
        }

    broadcastReceiverCaptor.value.onReceive(mockActivity, intent)
    verify(mockActivity).startActivityForResult(eq(extraConsentIntent), capture(requestCodeCaptor))

    val smsContent = "smsContentValue"
    val smsIntent = Intent().apply { putExtra(SmsRetriever.EXTRA_SMS_MESSAGE, smsContent) }
    otpSmsRetriever.handle(requestCodeCaptor.value, Activity.RESULT_OK, smsIntent)
    assertEquals(Ok(smsContent), smsResult)
  }

  @Test
  fun `test concurrent calls return error`() {
    var secondSmsResult: OtpSmsRetrieverResult? = null
    otpSmsRetriever.startListening {}

    otpSmsRetriever.startListening { secondSmsResult = it }

    assertEquals(Err(OtpSmsRetrieverError.CONCURRENT_CALL), secondSmsResult)
  }

  @Test
  fun `stopListening unregisters broadcast receiver`() {
    otpSmsRetriever.startListening {}
    verify(mockActivity)
        .registerReceiver(
            capture(broadcastReceiverCaptor), any(), eq(SmsRetriever.SEND_PERMISSION), eq(null))

    otpSmsRetriever.stopListening()
    verify(mockActivity).unregisterReceiver(broadcastReceiverCaptor.value)
  }
}
