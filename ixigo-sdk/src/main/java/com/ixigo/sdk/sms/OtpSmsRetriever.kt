package com.ixigo.sdk.sms

import android.app.Activity
import android.content.*
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.common.Result
import timber.log.Timber

typealias OtpSmsRetrieverResult = Result<String, OtpSmsRetrieverError>

typealias OtpSmsRetrieverCallback = (OtpSmsRetrieverResult) -> Unit

enum class OtpSmsRetrieverError {
  CONCURRENT_CALL,
  CONSENT_DENIED,
  SDK_ERROR
}

class OtpSmsRetriever(
    private val activity: Activity,
    private val smsRetrieverClient: SmsRetrieverClient = SmsRetriever.getClient(activity)
) : ActivityResultHandler {
  var callback: OtpSmsRetrieverCallback? = null

  fun startListening(callback: OtpSmsRetrieverCallback) {
    if (this.callback != null) {
      callback(Err(OtpSmsRetrieverError.CONCURRENT_CALL))
      return
    }
    this.callback = callback
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    activity.registerReceiver(
        smsVerificationReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null)
    smsRetrieverClient.startSmsUserConsent(null)
  }

  fun stopListening() {
    activity.unregisterReceiver(smsVerificationReceiver)
    this.callback = null
  }

  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    when (requestCode) {
      smsConsentRequest -> {
        // Obtain the phone number from the result
        if (resultCode == Activity.RESULT_OK && data != null) {
          // Get SMS message content
          val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
          if (message != null) {
            callback?.invoke(Ok(message))
          } else {
            callback?.invoke(Err(OtpSmsRetrieverError.SDK_ERROR))
          }
        } else {
          callback?.invoke(Err(OtpSmsRetrieverError.CONSENT_DENIED))
        }
        stopListening()
        return true
      }
    }
    return false
  }

  private val smsConsentRequest = 1101
  private val smsVerificationReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (smsRetrieverStatus.statusCode) {
              CommonStatusCodes.SUCCESS -> {
                val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                try {
                  activity.startActivityForResult(consentIntent, smsConsentRequest)
                } catch (e: ActivityNotFoundException) {
                  Timber.e(e, "Unable to start activity for Result in SMS Consent API")
                }
              }
              CommonStatusCodes.TIMEOUT -> {
                Timber.e("Timeout Receiveing SMS content")
              }
            }
          }
        }
      }
}
