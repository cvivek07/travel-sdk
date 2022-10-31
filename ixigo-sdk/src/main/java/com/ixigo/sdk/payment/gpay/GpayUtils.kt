package com.ixigo.sdk.payment.gpay

import android.app.Activity
import com.google.android.apps.nbu.paisa.inapp.client.api.PaymentsClient
import com.google.android.apps.nbu.paisa.inapp.client.api.Wallet
import com.ixigo.sdk.payment.data.GpayPaymentInput
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object GpayUtils {
  /**
   * Create a Google Pay API base request object with properties used in all requests.
   *
   * @return Google Pay API base request object.
   * @throws JSONException
   */
  private val baseRequest =
      JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
      }

  private fun getPaymentMethod(): JSONArray {
    val type = JSONObject().apply { put("type", "UPI") }
    return JSONArray().put(type)
  }

  /**
   * An object describing accepted forms of payment by your app, used to determine a viewer's
   * readiness to pay.
   * @return API version and payment methods supported by the app.
   */
  fun isReadyToPayRequest(): String? {
    return try {
      baseRequest.apply { put("allowedPaymentMethods", getPaymentMethod()) }.toString()
    } catch (e: JSONException) {
      null
    }
  }

  /**
   * Creates an instance of [PaymentsClient] for use in an [Activity] using the environment and
   * theme set in [Constants].
   */
  fun createPaymentsClient(): PaymentsClient {
    return Wallet.getPaymentsClient()
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status.
   *
   * @return information about the requested payment.
   * @throws JSONException
   * @see [TransactionInfo]
   */
  @Throws(JSONException::class)
  private fun getTransactionInfo(input: GpayPaymentInput): JSONObject {
    return JSONObject().apply {
      put("totalPrice", input.amount)
      put("totalPriceStatus", "FINAL")
      put("currencyCode", "INR")
      put("transactionNote", input.transactionNote)
    }
  }

  /**
   * An object describing information requested in a Google Pay payment sheet
   * @return Payment data expected by your app.
   * @see [PaymentDataRequest]
   */
  fun getPaymentDataRequest(input: GpayPaymentInput): String? {
    return try {
      baseRequest
          .apply {
            put("allowedPaymentMethods", JSONArray().put(getAllowedPaymentMethods(input)))
            put("transactionInfo", getTransactionInfo(input))
          }
          .toString()
    } catch (e: JSONException) {
      null
    }
  }

  private fun getAllowedPaymentMethods(input: GpayPaymentInput): JSONObject {
    return JSONObject().apply {
      put("type", "UPI")
      put("parameters", getParameters(input))
      put("tokenizationSpecification", JSONObject().apply { put("type", "DIRECT") })
    }
  }

  private fun getParameters(input: GpayPaymentInput): JSONObject {
    return JSONObject().apply {
      put("payeeVpa", input.payeeVpa)
      put("payeeName", input.payeeName)
      put("referenceUrl", input.referenceUrl)
      put("mcc", input.mcc)
      put("transactionReferenceId", input.transactionReferenceId) // paymentTransactionId
      put("transactionId", input.transactionId) // paymentId (used by gpay)
    }
  }
}
