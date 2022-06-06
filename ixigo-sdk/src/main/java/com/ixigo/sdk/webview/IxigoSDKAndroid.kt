import android.content.*
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.common.*
import com.ixigo.sdk.common.NativePromiseError.Companion.wrongInputError
import com.ixigo.sdk.sms.OtpSmsRetriever
import com.ixigo.sdk.sms.OtpSmsRetrieverError
import com.ixigo.sdk.webview.BackNavigationMode
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.UIConfig
import com.ixigo.sdk.webview.WebViewFragment
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

internal class IxigoSDKAndroid(
    private val analyticsProvider: AnalyticsProvider,
    private val fragment: WebViewFragment,
    private val otpSmsRetriever: OtpSmsRetriever = OtpSmsRetriever(fragment.requireActivity()),
    private val partnerTokenProvider: PartnerTokenProvider = IxigoSDK.instance.partnerTokenProvider,
    private val customChromeTabsHelper: CustomChromeTabsHelper = CustomChromeTabsHelper()
) : JsInterface, ActivityResultHandler {

  override val name: String
    get() = "IxigoSDKAndroid"

  private val factory =
      PolymorphicJsonAdapterFactory.of(BackNavigationMode::class.java, "type")
          .withSubtype(BackNavigationMode.Enabled::class.java, "enabled")
          .withSubtype(BackNavigationMode.Disabled::class.java, "disabled")
          .withSubtype(BackNavigationMode.Handler::class.java, "handler")

  private val moshi by lazy { Moshi.Builder().add(factory).add(KotlinJsonAdapterFactory()).build() }
  private val logEventInputAdapter by lazy { moshi.adapter(LogEventInput::class.java) }
  private val readSmsOutputAdapter by lazy { moshi.adapter(ReadSmsOutput::class.java) }
  private val errorAdapter by lazy { moshi.adapter(NativePromiseError::class.java) }
  private val fetchPartnerTokenInputAdapter by lazy {
    moshi.adapter(FetchPartnerTokenInput::class.java)
  }
  private val fetchPartnerTokenResponseAdapter by lazy {
    moshi.adapter(FetchPartnerTokenResponse::class.java)
  }

  private val openWindowOptionsAdapter by lazy { moshi.adapter(OpenWindowOptions::class.java) }

  @JavascriptInterface
  fun logEvent(jsonInput: String): Boolean {
    val logEventInput = kotlin.runCatching { logEventInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (logEventInput == null) {
      Timber.e("Error parsing logEvent json=$jsonInput")
      return false
    }
    fragment?.activity?.runOnUiThread {
      analyticsProvider.logEvent(
          Event(
              name = logEventInput.name,
              properties = logEventInput.properties,
              referrer = fragment.webView.url))
    }
    return true
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @JavascriptInterface
  fun readSms(success: String, error: String) {
    otpSmsRetriever.startListening {
      when (it) {
        is Err ->
            executeNativePromiseResponse(
                replaceNativePromisePayload(
                    error, nativePromiseErrorFromSmsOtpError(it.value), errorAdapter),
                fragment)
        is Ok -> {
          executeNativePromiseResponse(
              replaceNativePromisePayload(success, ReadSmsOutput(it.value), readSmsOutputAdapter),
              fragment)
        }
      }
    }
  }

  @JavascriptInterface
  fun openWindow(url: String, optionsString: String) {
    val openWindowOptions =
        try {
          openWindowOptionsAdapter.fromJson(optionsString)
              ?: throw Exception("Error parsing optionsString=$optionsString")
        } catch (e: Exception) {
          Timber.e("Error parsing optionsString=$optionsString. Using default options.")
          OpenWindowOptions()
        }
    val activity = fragment.requireActivity()
    activity.runOnUiThread {
      when (openWindowOptions.browser) {
        BrowserType.WEBVIEW -> IxigoSDK.instance.launchWebActivity(activity, url)
        BrowserType.NATIVE -> customChromeTabsHelper.openUrl(activity, url)
      }
    }
  }

  @JavascriptInterface
  fun openAdditionalBusTrips(): Boolean {
    return if (BusSDK.initialized) {
      BusSDK.instance.launchAdditionalTrips(fragment.requireContext())
      true
    } else {
      Timber.e("Unable to launch Bus Trips as BusSDK has not been initialized")
      false
    }
  }

  @JavascriptInterface
  fun configureUI(jsonInput: String, success: String, error: String) {
    val input =
        kotlin.runCatching { moshi.adapter(UIConfig::class.java).fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      executeNativePromiseResponse(
          replaceNativePromisePayload(error, wrongInputError(jsonInput), errorAdapter), fragment)
      return
    }
    fragment.requireActivity().runOnUiThread {
      fragment.configUI(input)
      executeNativePromiseResponse(replaceNativePromisePayload(success, "{}"), fragment)
    }
  }

  @JavascriptInterface
  fun fetchPartnerToken(jsonInput: String, success: String, error: String) {
    val input = kotlin.runCatching { fetchPartnerTokenInputAdapter.fromJson(jsonInput) }.getOrNull()
    if (input == null) {
      executeNativePromiseResponse(
          replaceNativePromisePayload(error, wrongInputError(jsonInput), errorAdapter), fragment)
      return
    }
    partnerTokenProvider.fetchPartnerToken(
        fragment.requireActivity(),
        PartnerTokenProvider.Requester(
            input.partnerId, PartnerTokenProvider.RequesterType.CUSTOMER)) {
      when (it) {
        is Err -> {
          val nativeError =
              NativePromiseError(
                  errorCode = it.value.code.toString(), errorMessage = it.value.message)
          executeNativePromiseResponse(
              replaceNativePromisePayload(error, nativeError, errorAdapter), fragment)
        }
        is Ok -> {
          val response = FetchPartnerTokenResponse(authToken = it.value.token)
          executeNativePromiseResponse(
              replaceNativePromisePayload(
                  success, fetchPartnerTokenResponseAdapter.toJson(response)),
              fragment)
        }
      }
    }
  }

  @Keep data class LogEventInput(val name: String, val properties: Map<String, String> = mapOf())

  @Keep data class ReadSmsOutput(val smsContent: String)

  @Keep data class FetchPartnerTokenInput(val partnerId: String)
  @Keep data class FetchPartnerTokenResponse(val authToken: String)

  @Keep data class OpenWindowOptions(val browser: BrowserType = BrowserType.WEBVIEW)

  @Keep
  enum class BrowserType {
    @Json(name = "native") NATIVE,
    @Json(name = "browser") WEBVIEW
  }

  override fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
      otpSmsRetriever.handle(requestCode, resultCode, data)

  private fun nativePromiseErrorFromSmsOtpError(
      otpSmsRetrieverError: OtpSmsRetrieverError
  ): NativePromiseError {
    return when (otpSmsRetrieverError) {
      OtpSmsRetrieverError.CONCURRENT_CALL -> NativePromiseError("ConcurrentCall")
      OtpSmsRetrieverError.CONSENT_DENIED -> NativePromiseError("ConsentDenied")
      OtpSmsRetrieverError.SDK_ERROR -> NativePromiseError("SDKError")
    }
  }
}
