package com.ixigo.sdk.payment

import android.app.Activity.*
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.apps.nbu.paisa.inapp.client.api.WalletConstants
import com.google.android.apps.nbu.paisa.inapp.client.api.WalletUtils
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.common.*
import com.ixigo.sdk.common.NativePromiseError.Companion.notAvailableError
import com.ixigo.sdk.common.NativePromiseError.Companion.sdkError
import com.ixigo.sdk.common.NativePromiseError.Companion.wrongInputError
import com.ixigo.sdk.payment.PackageManager.Companion.PHONEPE_PACKAGE_NAME
import com.ixigo.sdk.payment.PackageManager.Companion.REQUEST_CODE_GPAY_APP
import com.ixigo.sdk.payment.PackageManager.Companion.REQUEST_CODE_PHONEPE_APP
import com.ixigo.sdk.payment.PackageManager.Companion.REQUEST_CODE_UPI_APP
import com.ixigo.sdk.payment.data.*
import com.ixigo.sdk.payment.gpay.*
import com.ixigo.sdk.payment.minkasu_sdk.MinkasuSDKManager
import com.ixigo.sdk.payment.viewmodel.PaymentViewModel
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import com.ixigo.sdk.webview.WebViewFragmentListener
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

internal class PaymentJsInterface(
    private val webViewFragment: WebViewFragment,
    gatewayProvider: PaymentGatewayProvider,
    private val gPayClientFactory: GPayClientFactory,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : JsInterface, WebViewFragmentListener, ActivityResultHandler {
  override val name: String = "PaymentSDKAndroid"

  private val cachingGatewayProvider =
      CachingPaymentGatewayProvider(webViewFragment.requireActivity(), gatewayProvider)

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val inputAdapter by lazy { moshi.adapter(InitializeInput::class.java) }
  private val availableUpiAppsInputAdapter by lazy {
    moshi.adapter(GetAvailableUPIAppsInput::class.java)
  }
  private val availableUpiAppsResponseAdapter by lazy {
    moshi.adapter(GetAvailableUPIAppsResponse::class.java)
  }
  private val processUpiIntentInputAdapter by lazy {
    moshi.adapter(ProcessUpiIntentInput::class.java)
  }
  private val processGatewayPaymentResponseAdapter by lazy {
    moshi.adapter(ProcessGatewayPaymentResponse::class.java)
  }
  private val finishPaymentInputAdapter by lazy { moshi.adapter(FinishPaymentInput::class.java) }
  private val finishPaymentResponseAdapter by lazy {
    moshi.adapter(FinishPaymentResponse::class.java)
  }
  private val errorAdapter by lazy { moshi.adapter(NativePromiseError::class.java) }

  @VisibleForTesting val paymentViewModel: PaymentViewModel by webViewFragment.viewModels()

  private val packageManager: PackageManager by lazy {
    PackageManager(webViewFragment.requireContext().applicationContext)
  }

  private val paymentsClient by lazy { gPayClientFactory.create(webViewFragment.requireContext()) }

  private var minkasuInput: MinkasuInput? = null

  private val webViewCallback =
      object : WebViewCallback {
        override fun onWebViewAvailable(webView: WebView) {
          minkasuInput?.let { MinkasuSDKManager(webViewFragment, webView).initMinkasu2FASDK(it) }
        }
      }

  @JavascriptInterface
  fun initialize(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { inputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError", errorMessage = "Payment already initialized")
      returnError(error, errorPayload)
      return
    }

    gateway.initialize(input) {
      when (it) {
        is Err -> {
          val errorPayload =
              NativePromiseError(
                  errorCode = it.value.errorCode, errorMessage = it.value.errorMessage)
          returnError(error, errorPayload)
        }
        is Ok -> {
          executeResponse(replaceNativePromisePayload(success, "{}"))
        }
      }
    }
  }

  @JavascriptInterface
  fun getAvailableUPIApps(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { availableUpiAppsInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }

    gateway.listAvailableUPIApps(input) {
      when (it) {
        is Err -> {
          returnError(error, it.value)
        }
        is Ok -> {
          executeResponse(
              replaceNativePromisePayload(success, it.value, availableUpiAppsResponseAdapter))
        }
      }
    }
  }

  @JavascriptInterface
  fun getUPIDirectApps(success: String, error: String) {
    try {
      val packageManager: android.content.pm.PackageManager =
          webViewFragment.requireContext().packageManager
      val mainIntent = Intent(Intent.ACTION_MAIN, null)
      mainIntent.addCategory(Intent.CATEGORY_DEFAULT)
      mainIntent.addCategory(Intent.CATEGORY_BROWSABLE)
      mainIntent.action = Intent.ACTION_VIEW
      val uri = Uri.Builder().scheme("upi").authority("pay").build()
      mainIntent.data = uri
      val pkgAppsList: MutableList<ResolveInfo>? =
          webViewFragment.context?.packageManager?.queryIntentActivities(mainIntent, 0)

      val list = arrayListOf<UpiApp>()
      for (i in pkgAppsList?.indices!!) {
        val resolveInfo: ResolveInfo = pkgAppsList[i]
        val app =
            UpiApp(
                resolveInfo.loadLabel(packageManager).toString(),
                resolveInfo.activityInfo.packageName)
        list.add(app)
      }
      val response = GetAvailableUPIAppsResponse(list)
      executeResponse(
          replaceNativePromisePayload(success, response, availableUpiAppsResponseAdapter))
    } catch (e: Exception) {
      e.printStackTrace()
      returnError(error, notAvailableError())
    }
  }

  @JavascriptInterface
  fun launchUPIApp(jsonInput: String, success: String, error: String) {
    val input =
        kotlin
            .runCatching { moshi.adapter(LaunchUPIApp::class.java).fromJson(jsonInput) }
            .getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    webViewFragment.requireActivity().runOnUiThread {
      paymentViewModel.upiDirectResultMutableLiveData.observe(webViewFragment) {
        if (it != null) {
          executeResponse(
              replaceNativePromisePayload(
                  if (it.paymentFinished) success else error,
                  it,
                  moshi.adapter(PaymentFinished::class.java)))
          paymentViewModel.upiDirectResultMutableLiveData.postValue(null)
        }
      }
    }
    try {
      val i =
          Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(input.deeplink)
            `package` = input.packageName
          }
      webViewFragment.requireActivity().startActivityForResult(i, REQUEST_CODE_UPI_APP)
    } catch (e: ActivityNotFoundException) {
      returnError(error, notAvailableError())
    }
  }

  @JavascriptInterface
  fun processUPIIntent(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { processUpiIntentInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }

    gateway.processUpiIntent(input) {
      when (it) {
        is Err -> {
          returnError(error, it.value)
        }
        is Ok -> {
          executeResponse(
              replaceNativePromisePayload(success, it.value, processGatewayPaymentResponseAdapter))
        }
      }
    }
  }

  /** Generic method to fetch response from juspay sdk */
  @JavascriptInterface
  fun process(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { JSONObject(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val provider = "JUSPAY"
    val gateway = cachingGatewayProvider.getPaymentGateway(provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }
    gateway.setCallback(webViewCallback)
    gateway.process(input) {
      executeResponse(replaceNativePromisePayload(success, escapeSpecialCharacters(it.toString())))
    }
  }

  @JavascriptInterface
  fun finishPayment(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { finishPaymentInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    if (PaymentSDK.instance.finishPayment(input)) {
      if (webViewFragment.quitPaymentPage) webViewFragment.delegate?.onQuit()
      executeResponse(
          replaceNativePromisePayload(
              success,
              FinishPaymentResponse(handler = PaymentHandler.NATIVE),
              finishPaymentResponseAdapter))
    } else {
      returnError(error, sdkError("Unable to find transactionId=${input.transactionId}"))
    }
  }

  @JavascriptInterface
  fun updateTransactionId(oldTransactionId: String, newTransactionId: String) {
    val currentTransactions = PaymentSDK.instance.currentTransactions
    val value = currentTransactions.remove(oldTransactionId)
    value?.let { currentTransactions[newTransactionId] = value }
  }

  @JavascriptInterface
  fun checkCredEligibility(jsonInput: String, success: String, error: String) {
    val enabled = packageManager.isCredAppInstalled()
    executeResponse(
        replaceNativePromisePayload(
            success,
            CredEligibilityResponse(enabled),
            moshi.adapter(CredEligibilityResponse::class.java)))
  }

  @JavascriptInterface
  fun processCredPayment(jsonInput: String, success: String, error: String) {
    val input =
        kotlin
            .runCatching { moshi.adapter(ProcessCredPaymentInput::class.java).fromJson(jsonInput) }
            .getOrNull()
    if (input == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
    val gateway = cachingGatewayProvider.getPaymentGateway(input.provider)
    if (gateway == null) {
      val errorPayload =
          NativePromiseError(
              errorCode = "InvalidArgumentError",
              errorMessage = "Could not find payment provider=${input.provider}")
      returnError(error, errorPayload)
      return
    }
    if (!gateway.initialized) {
      val errorPayload =
          NativePromiseError(
              errorCode = "NotInitializedError",
              errorMessage = "Call `PaymentSDKAndroid.initialize` before calling this method")
      returnError(error, errorPayload)
      return
    }

    gateway.processCredPayment(input) {
      when (it) {
        is Err -> {
          returnError(error, it.value)
        }
        is Ok -> {
          executeResponse(
              replaceNativePromisePayload(success, it.value, processGatewayPaymentResponseAdapter))
        }
      }
    }
  }

  @JavascriptInterface
  @Suppress("UNUSED_PARAMETER")
  fun scanCreditCard(success: String, error: String) {
    returnError(error, notAvailableError())
  }

  /**
   * Checks if PhonePe App is installed and enabled on the device. Sends response back to js using
   * webView.evaluateJavascript method
   */
  @JavascriptInterface
  fun isPhonePeUpiAvailable(success: String, error: String) {
    val enabled = packageManager.isPhonePeUpiAvailable()
    executeResponse(
        replaceNativePromisePayload(
            success,
            PhonePeAvailabilityResponse(enabled),
            moshi.adapter(PhonePeAvailabilityResponse::class.java)))
  }

  /** If PhonePe app is installed, returns the version code */
  @JavascriptInterface
  fun getPhonePeVersionCode(success: String, error: String) {
    val phonePeVersionCode = packageManager.extractPhonePeVersionCode()
    executeResponse(
        replaceNativePromisePayload(
            success,
            PhonePeVersionCode(phonePeVersionCode),
            moshi.adapter(PhonePeVersionCode::class.java)))
  }

  /** Gets the data from js-sdk and launches the phonepe app using deeplink url. */
  @JavascriptInterface
  fun getPhonePeRedirectData(jsonInput: String, success: String, error: String) {
    val input =
        kotlin
            .runCatching { moshi.adapter(PhonePeRedirectData::class.java).fromJson(jsonInput) }
            .getOrNull()
            ?: return

    webViewFragment.requireActivity().runOnUiThread {
      paymentViewModel.phonePeResultMutableLiveData.observe(webViewFragment) {
        executeResponse(
            replaceNativePromisePayload(
                success, it, moshi.adapter(PhonePePaymentFinished::class.java)))
      }
    }

    when (input.redirectType) {
      "WEB" -> (webViewFragment.requireActivity() as WebActivity).loadUrl(input.redirectUrl)
      "INTENT" -> {
        try {
          val i =
              Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(input.redirectUrl)
                `package` = PHONEPE_PACKAGE_NAME
              }
          webViewFragment.requireActivity().startActivityForResult(i, REQUEST_CODE_PHONEPE_APP)
        } catch (e: ActivityNotFoundException) {
          returnError(error, notAvailableError())
        }
      }
    }
  }

  /**
   * Determine the viewer's ability to pay with a payment method supported by your app and display a
   * Google Pay payment button.
   */
  @JavascriptInterface
  fun isGpayUpiAvailable(success: String, error: String) {
    webViewFragment.lifecycleScope.launch {
      try {
        val isReady = paymentsClient.isReadyToPay()
        executeResponse(
            replaceNativePromisePayload(
                success,
                JuspayPaymentMethodsEligibility(isReady),
                moshi.adapter(JuspayPaymentMethodsEligibility::class.java)))
      } catch (e: Exception) {
        executeResponse(
            replaceNativePromisePayload(
                success,
                JuspayPaymentMethodsEligibility(false),
                moshi.adapter(JuspayPaymentMethodsEligibility::class.java)))
        Timber.e(e)
      }
    }
  }

  /** Launch gpay app */
  @JavascriptInterface
  fun requestGpayPayment(jsonInput: String, success: String, error: String) {
    webViewFragment.lifecycleScope.launch {
      try {
        val input =
            kotlin
                .runCatching { moshi.adapter(GpayPaymentInput::class.java).fromJson(jsonInput) }
                .getOrNull()
        if (input == null) {
          returnError(error, wrongInputError(jsonInput))
          return@launch
        }

        withContext(mainDispatcher) {
          paymentViewModel.gpayResultMutableLiveData.observe(webViewFragment) {
            if (it != null) {
              if (it.paymentFinished) {
                executeResponse(
                    replaceNativePromisePayload(
                        success, it, moshi.adapter(PaymentFinished::class.java)))
              } else {
                executeResponse(
                    replaceNativePromisePayload(
                        error, it, moshi.adapter(PaymentFinished::class.java)))
              }
              paymentViewModel.gpayResultMutableLiveData.value = null
            }
          }
        }

        paymentsClient.loadPaymentData(
            webViewFragment.requireActivity(), input, REQUEST_CODE_GPAY_APP)
      } catch (e: Exception) {
        returnError(error, sdkError("Gpay Payment Error"))
        Timber.e(e)
      }
    }
  }

  /** Get ixigo sdk version */
  @JavascriptInterface
  fun getIxigoSDKVersion(success: String, error: String) {
    val sdkVersion = BuildConfig.SDK_VERSION
    executeResponse(
        replaceNativePromisePayload(
            success, IxigoSDKVersion(sdkVersion), moshi.adapter(IxigoSDKVersion::class.java)))
  }

  /** Get minkasu data from js-sdk & use it to initialize Minkasu sdk */
  @JavascriptInterface
  fun initializeMinkasuSDK(jsonInput: String, success: String, error: String) {
    minkasuInput =
        kotlin
            .runCatching { moshi.adapter(MinkasuInput::class.java).fromJson(jsonInput) }
            .getOrNull()
    if (minkasuInput == null) {
      returnError(error, wrongInputError(jsonInput))
      return
    }
  }

  private fun returnError(error: String, errorPayload: NativePromiseError) {
    executeResponse(replaceNativePromisePayload(error, errorPayload, errorAdapter))
  }

  private fun executeResponse(message: String) {
    executeNativePromiseResponse(message, webViewFragment)
  }

  override fun onUrlLoadStart(webViewFragment: WebViewFragment, url: String?) {
    cachingGatewayProvider.clear()
  }

  override fun onBackPressed(webViewFragment: WebViewFragment): Boolean {
    try {
      val provider = "JUSPAY"
      val gateway = cachingGatewayProvider.getPaymentGateway(provider)
      if (gateway != null && gateway.initialized) {
        return gateway.onBackPressed()
      }
    } catch (e: Exception) {
      Timber.e(e)
    }
    return false
  }

  /** Callback for onActivityResult */
  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    when (requestCode) {
      REQUEST_CODE_PHONEPE_APP -> {
        if (resultCode != RESULT_CANCELED) {
          paymentViewModel.setPhonePeResult(PhonePePaymentFinished(paymentFinished = true))
        }
      }
      REQUEST_CODE_GPAY_APP -> {
        when (resultCode) {
          RESULT_OK -> {
            val paymentData = WalletUtils.getPaymentDataFromIntent(data)
            paymentViewModel.setGpayPaymentResult(PaymentFinished(true))
            Timber.d(paymentData)
          }
          RESULT_FIRST_USER -> {
            Timber.d(data.toString())
            val statusCode =
                data!!.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, WalletConstants.INTERNAL_ERROR)
            paymentViewModel.setGpayPaymentResult(PaymentFinished(false))
            handleResultStatusCode(statusCode)
          }
          RESULT_CANCELED -> {
            paymentViewModel.setGpayPaymentResult(PaymentFinished(false))
            Timber.d("User cancelled gpay transaction")
          }
        }
      }
      REQUEST_CODE_UPI_APP -> {
        Timber.d("UPI RESULT REQUEST CODE-->$requestCode")
        Timber.d("UPI RESULT RESULT CODE-->$resultCode")
        Timber.d("UPI RESULT DATA-->$data")
        when (resultCode) {
          RESULT_OK -> {
            // success
            paymentViewModel.setUPIDirectPaymentResult(PaymentFinished(true))
          }
          else -> {
            // failure
            paymentViewModel.setUPIDirectPaymentResult(PaymentFinished(false))
          }
        }
      }
    }
    return false
  }

  /** Handle gpay result status code */
  private fun handleResultStatusCode(statusCode: Int) {
    when (statusCode) {
      WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR -> {}
      WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR -> {}
      WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION,
      WalletConstants.INTERNAL_ERROR,
      WalletConstants.DEVELOPER_ERROR -> throw IllegalStateException("Internal error.")
      else -> throw IllegalStateException("Internal error.")
    }
  }
}
