package com.ixigo.sdk.payment.gpay

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.nbu.paisa.inapp.client.api.PaymentsClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GPayClientTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val mockGPaymentsClient = mock<PaymentsClient>()
  private val mockCompletionTask = mock<Task<Boolean>>()
  private val gPayClient = GPayClient(context, mockGPaymentsClient)

  @Test
  fun `isReadyToPay returns true when gpay client is ready`() = runTest {
    val completionListenerCaptor = argumentCaptor<OnCompleteListener<Boolean>>()
    Mockito.`when`(mockCompletionTask.getResult<Exception>(any())).thenReturn(true)
    Mockito.`when`(mockCompletionTask.addOnCompleteListener(completionListenerCaptor.capture()))
        .thenAnswer {
          val listener = completionListenerCaptor.firstValue
          listener.onComplete(mockCompletionTask)
          return@thenAnswer mockCompletionTask
        }
    Mockito.`when`(mockGPaymentsClient.isReadyToPay(any(), any())).thenReturn(mockCompletionTask)

    val result = gPayClient.isReadyToPay()
    assertEquals(true, result)
  }

  @Test
  fun `isReadyToPay returns false when gpay client is not ready`() = runTest {
    val completionListenerCaptor = argumentCaptor<OnCompleteListener<Boolean>>()
    Mockito.`when`(mockCompletionTask.getResult<Exception>(any())).thenReturn(false)
    Mockito.`when`(mockGPaymentsClient.isReadyToPay(any(), any())).thenReturn(mockCompletionTask)
    Mockito.`when`(mockCompletionTask.addOnCompleteListener(completionListenerCaptor.capture()))
        .thenAnswer {
          val listener = completionListenerCaptor.firstValue
          listener.onComplete(mockCompletionTask)
          return@thenAnswer mockCompletionTask
        }

    val result = gPayClient.isReadyToPay()
    assertEquals(false, result)
  }

  @Test(expected = ApiException::class)
  fun `isReadyToPay throws exception when gpay client fails to resolve successfully`() = runTest {
    val completionListenerCaptor = argumentCaptor<OnCompleteListener<Boolean>>()
    Mockito.`when`(mockCompletionTask.getResult<Exception>(any()))
        .thenThrow(ApiException(Status.RESULT_TIMEOUT))
    Mockito.`when`(mockGPaymentsClient.isReadyToPay(any(), any())).thenReturn(mockCompletionTask)
    Mockito.`when`(mockCompletionTask.addOnCompleteListener(completionListenerCaptor.capture()))
        .thenAnswer {
          val listener = completionListenerCaptor.firstValue
          listener.onComplete(mockCompletionTask)
          return@thenAnswer mockCompletionTask
        }

    gPayClient.isReadyToPay()
  }
}
