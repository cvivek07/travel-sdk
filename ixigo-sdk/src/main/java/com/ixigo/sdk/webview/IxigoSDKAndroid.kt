import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.WebViewFragment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber

internal class IxigoSDKAndroid(
    private val analyticsProvider: AnalyticsProvider,
    private val fragment: WebViewFragment
) : JsInterface {

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val logEventInputAdapter by lazy { moshi.adapter(LogEventInput::class.java) }

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

  override val name: String
    get() = "IxigoSDKAndroid"

  @Keep data class LogEventInput(val name: String, val properties: Map<String, String> = mapOf())
}
