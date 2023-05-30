package com.ixigo.sdk.payment.simpl

import android.content.Context
import com.ixigo.sdk.common.SdkNotFoundException
import com.simpl.android.fingerprint.SimplFingerprint
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SimplClient
constructor(
    private val context: Context,
    private val factory: SimplFingerPrintFactory = SimplFingerPrintFactory(context)
) {

  @Throws(SdkNotFoundException::class)
  suspend fun getFingerPrint(customerMobile: String, customerEmail: String): String? {
    checkExists()

    val instance = factory.create(customerMobile, customerEmail)
    return suspendCoroutine { instance.generateFingerprint { payload -> it.resume(payload) } }
  }

  companion object {

    @Throws(SdkNotFoundException::class)
    fun checkExists() {
      val className = "com.simpl.android.fingerprint.SimplFingerprint"
      try {
        Class.forName(className)
      } catch (e: ClassNotFoundException) {
        throw SdkNotFoundException()
      }
    }
  }
}

class SimplFingerPrintFactory constructor(private val context: Context) {

  fun create(primaryId: String, secondaryId: String): SimplFingerprint {
    SimplFingerprint.init(context, primaryId, secondaryId)
    return SimplFingerprint.getInstance()
  }
}
