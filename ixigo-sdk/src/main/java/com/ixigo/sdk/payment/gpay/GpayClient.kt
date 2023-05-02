package com.ixigo.sdk.payment.gpay

import android.app.Activity
import android.content.Context
import com.google.android.apps.nbu.paisa.inapp.client.api.PaymentsClient
import com.google.android.gms.common.api.ApiException
import com.ixigo.sdk.payment.data.GpayPaymentInput
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GPayClientFactory {
  fun create(context: Context): GPayClient {
    return GPayClient(context)
  }
}

class GPayClient
constructor(
    private val context: Context,
    private val paymentsClient: PaymentsClient = GpayUtils.createPaymentsClient()
) {

  @Throws(ApiException::class)
  suspend fun isReadyToPay(): Boolean {
    val isReadyToPayJson = GpayUtils.isReadyToPayRequest() ?: return false
    return suspendCoroutine { continuation ->
      val task = paymentsClient.isReadyToPay(context, isReadyToPayJson)
      task.addOnCompleteListener { completedTask ->
        try {
          completedTask.getResult(ApiException::class.java)?.let { continuation.resume(it) }
        } catch (exception: ApiException) {
          continuation.resumeWithException(exception)
        }
      }
    }
  }

  fun loadPaymentData(activity: Activity, paymentInput: GpayPaymentInput, requestCode: Int) {
    val paymentDataRequestJson = GpayUtils.getPaymentDataRequest(paymentInput)
    paymentsClient.loadPaymentData(activity, paymentDataRequestJson, requestCode)
  }
}
