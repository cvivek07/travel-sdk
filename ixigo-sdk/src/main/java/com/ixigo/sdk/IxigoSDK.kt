package com.ixigo.sdk

import IxigoSDKAndroid
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.test.espresso.idling.net.UriIdlingResource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ixigo.sdk.Config.Companion.ProdConfig
import com.ixigo.sdk.IxigoSDK.Companion.init
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.CachingPartnerTokenProvider
import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.SdkSingleton
import com.ixigo.sdk.flights.FlightSearchData
import com.ixigo.sdk.flights.getFlightsSearchParams
import com.ixigo.sdk.payment.DefaultPaymentProvider
import com.ixigo.sdk.payment.PaymentProvider
import com.ixigo.sdk.remoteConfig.FirebaseRemoteConfigProvider
import com.ixigo.sdk.remoteConfig.RemoteConfigProvider
import com.ixigo.sdk.ui.Theme
import com.ixigo.sdk.ui.defaultTheme
import com.ixigo.sdk.webview.*
import java.net.URL
import timber.log.Timber

/**
 * This is the main entrypoint to interact with Ixigo SDK.
 *
 * All interactions should happen via its singleton object, [IxigoSDK.instance][getInstance].
 *
 * Before using it, you need to call [IxigoSDK.init(...)][init] once when you start-up your
 * Application.
 */
class IxigoSDK
internal constructor(
    internal val appInfo: AppInfo,
    partnerTokenProvider: PartnerTokenProvider,
    internal val paymentProvider: PaymentProvider,
    internal val analyticsProvider: AnalyticsProvider,
    internal val config: Config = ProdConfig,
    internal val webViewConfig: WebViewConfig = WebViewConfig(),
    internal val deeplinkHandler: DeeplinkHandler? = null,
    internal val theme: Theme,
    internal val remoteConfigProvider: RemoteConfigProvider
) : JsInterfaceProvider {

  private val cachingPartnerTokenProvider: CachingPartnerTokenProvider =
      CachingPartnerTokenProvider(partnerTokenProvider)

  internal val partnerTokenProvider: PartnerTokenProvider
    get() = cachingPartnerTokenProvider

  @VisibleForTesting
  val uriIdlingResource: UriIdlingResource by lazy {
    UriIdlingResource("IxigoSDKUriIdlingResource", 1000)
  }

  internal val partnerToken: PartnerToken?
    get() = cachingPartnerTokenProvider.partnerToken

  companion object : SdkSingleton<IxigoSDK>("IxigoSDK") {

    /**
     * Initializes IxigoSDK with required parameters. This method needs to be called before
     * accessing the singleton via [getInstance]
     *
     * Call this method when you initialize your Application. eg: `Application.onCreate`
     *
     * @param context Android Context. Typically [ApplicationContext]
     * @param authProvider Delegates Authentication logic via this [AuthProvider]
     * @param paymentProvider Delegates Payment logic via this [PaymentProvider]
     * @param appInfo The [AppInfo]
     * @param analyticsProvider [AnalyticsProvider] used throughout the SDK
     * @param config The [Config] used to instantiate the Ixigo SDK
     * @param theme The [Theme] used to instantiate the Ixigo SDK
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        appInfo: AppInfo,
        partnerTokenProvider: PartnerTokenProvider,
        paymentProvider: PaymentProvider? = null,
        analyticsProvider: AnalyticsProvider? = null,
        config: Config = ProdConfig,
        theme: Theme = defaultTheme(context),
        deeplinkHandler: DeeplinkHandler? = null
    ): IxigoSDK {
      return internalInit(
          context,
          appInfo,
          partnerTokenProvider,
          paymentProvider,
          commonAnalyticsProvider(context, analyticsProvider),
          config,
          deeplinkHandler,
          theme)
    }

    internal fun internalInit(
        context: Context,
        appInfo: AppInfo,
        partnerTokenProvider: PartnerTokenProvider,
        paymentProvider: PaymentProvider?,
        analyticsProvider: AnalyticsProvider,
        config: Config = ProdConfig,
        deeplinkHandler: DeeplinkHandler? = null,
        theme: Theme = defaultTheme(context),
        remoteConfigProvider: RemoteConfigProvider =
            FirebaseRemoteConfigProvider(getFirebaseRemoteConfig(context, appInfo))
    ): IxigoSDK {

      assertNotCreated()
      val ixigoSDK =
          IxigoSDK(
              appInfo.replaceDefaults(UUIDFactory(context), DeviceIdFactory(context)),
              partnerTokenProvider,
              DefaultPaymentProvider(remoteConfigProvider, paymentProvider),
              analyticsProvider,
              config,
              deeplinkHandler = deeplinkHandler,
              theme = theme,
              remoteConfigProvider = remoteConfigProvider)
      INSTANCE = ixigoSDK

      ixigoSDK.webViewConfig.addJsInterfaceProvider(ixigoSDK)

      analyticsProvider.logEvent(
          Event(
              name = "sdkInit",
              properties =
                  mapOf("clientId" to appInfo.clientId, "sdkVersion" to BuildConfig.SDK_VERSION)))

      return ixigoSDK
    }

    private fun getFirebaseApp(context: Context, appInfo: AppInfo): FirebaseApp {
      val applicationId =
          when (appInfo.clientId) {
            "abhibus" -> "1:132902544575:android:9270763a13b544f571120a"
            "confirmtckt" -> "1:132902544575:android:956347285b51bc9771120a"
            "iximatr" -> "1:132902544575:android:6a2d77a68dc54bb371120a"
            "iximaad" -> "1:132902544575:android:4d24ef043c99312071120a\n"
            else -> {
              Timber.w("No Firebase App Id configured for clientId=${appInfo.clientId}")
              "1:132902544575:ios:f0d90487b4debd2971120a"
            }
          }
      val options =
          FirebaseOptions.Builder()
              .setProjectId("ixigo-sdk-demo-app")
              .setApplicationId(applicationId)
              .setApiKey("AIzaSyBEJrf3SjSFMUBahV5eL20pquCW6auVSfA")
              .build()

      if (kotlin.runCatching { Firebase.app("ixigo-sdk") }.getOrNull() == null) {
        Firebase.initialize(context, options, "ixigo-sdk")
      }
      return Firebase.app("ixigo-sdk")
    }

    private fun getFirebaseRemoteConfig(context: Context, appInfo: AppInfo): FirebaseRemoteConfig {
      val remoteConfig = Firebase.remoteConfig(getFirebaseApp(context, appInfo))
      val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }

      remoteConfig.setConfigSettingsAsync(configSettings)
      remoteConfig.fetchAndActivate().addOnSuccessListener {
        Timber.d("XXX test_key=${remoteConfig.getString("test_key")} clientId=${appInfo.clientId}")
        Timber.d("Successfully updated remote config")
      }
      remoteConfig.fetchAndActivate().addOnFailureListener {
        Timber.e(it, "Error updating remote config")
      }
      return remoteConfig
    }
  }

  /**
   * Opens an Activity to the specified url. It will add all necessary headers based on the provided
   * [AppInfo]
   *
   * @param context
   * @param url url to open the activity
   */
  internal fun launchWebActivity(context: Context, url: String, config: FunnelConfig? = null) {
    val intent = Intent(context, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
    config?.let { intent.putExtra(WebViewFragment.CONFIG, it) }
    context.startActivity(intent)
  }

  internal fun getHeaders(url: String): Map<String, String> {
    if (!isIxigoUrl(url)) {
      return mapOf()
    }
    val headers =
        mutableMapOf(
            "appVersion" to appInfo.appVersionString,
            "clientId" to appInfo.clientId,
            "apiKey" to appInfo.apiKey,
            "deviceId" to appInfo.deviceId,
            "uuid" to appInfo.uuid)
    partnerToken?.let { headers.put("Authorization", it.token) }
    return headers
  }

  /**
   * Call this method when the user logs out of the Host App This method will remove every
   * cached/stored user info in the SDK
   */
  fun onLogout() {
    cachingPartnerTokenProvider.clear()
    CookieManager.getInstance().removeAllCookies(null)
    webViewConfig.webStorage.deleteAllData()
  }

  internal fun getUrl(properties: Map<String, String>): String {
    val builder =
        Uri.parse(config.apiBaseUrl)
            .buildUpon()
            .appendPath("pwa")
            .appendPath("initialpage")
            .appendQueryParameter("clientId", appInfo.clientId)
            .appendQueryParameter("apiKey", appInfo.apiKey)
            .appendQueryParameter("appVersion", appInfo.appVersionString)
            .appendQueryParameter("deviceId", appInfo.deviceId)
            .appendQueryParameter("languageCode", "en") // TODO
    for (property in properties) {
      builder.appendQueryParameter(property.key, property.value)
    }
    return builder.build().toString()
  }

  private fun isIxigoUrl(url: String): Boolean {
    return try {
      URL(url).host?.endsWith("ixigo.com") ?: false
    } catch (e: Exception) {
      false
    }
  }

  override fun getJsInterfaces(url: String, webViewFragment: WebViewFragment): List<JsInterface> {
    val jsInterfaces = mutableListOf<JsInterface>()
    if (url.startsWith(config.apiBaseUrl) ||
        url.startsWith("file://") ||
        Uri.parse(url)?.host?.endsWith("ixigo.com") == true) {
      jsInterfaces.add(IxiWebView(webViewFragment))
    }
    jsInterfaces.add(IxigoSDKAndroid(analyticsProvider, webViewFragment))
    return jsInterfaces
  }

  // Flights
  // TODO: Move into its own SDK

  /**
   * Open Flights Home
   *
   * @param context
   */
  fun flightsStartHome(context: Context) {
    val url = getUrl(mapOf("page" to "FLIGHT_HOME"))
    launchWebActivity(context, url)
    analyticsProvider.logEvent(Event.with(action = "flightsStartHome"))
  }

  /**
   * Open Flights Trips
   *
   * @param context
   */
  fun flightsStartTrips(context: Context) {
    val url = getUrl(mapOf("page" to "FLIGHT_TRIPS"))
    launchWebActivity(context, url)
    analyticsProvider.logEvent(Event.with(action = "flightsStartTrips"))
  }

  /**
   * Use this to get a view displaying customer's flight trips and add it to your view hierarchy
   *
   * @return Fragment containing customer's flight trips
   */
  fun flightsTripsFragment(): Fragment {
    val arguments =
        Bundle().apply {
          val url = getUrl(mapOf("page" to "FLIGHT_TRIPS", "displayMode" to "embedded"))
          putParcelable(
              WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
          putParcelable(WebViewFragment.CONFIG, FunnelConfig(enableExitBar = false))
        }

    return WebViewFragment().apply { this.arguments = arguments }
  }

  /**
   * Starts a view containing flight search results
   *
   * @param context
   * @param searchData
   */
  fun flightsStartSearch(context: Context, searchData: FlightSearchData) {
    val url = getUrl(getFlightsSearchParams("FLIGHT_LISTING", searchData))
    launchWebActivity(context, url)

    analyticsProvider.logEvent(Event.with(action = "flightsStartSearch"))
  }

  /**
   * Use this to get a view displaying flight search results and add it to your view hierarchy
   *
   * @param searchData
   * @return Fragment containing customer's flight trips
   */
  fun flightsMultiModelFragment(searchData: FlightSearchData): Fragment {
    val arguments =
        Bundle().apply {
          val url = getUrl(getFlightsSearchParams("FLIGHT_LISTING_MULTI_MODEL", searchData))
          putParcelable(
              WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
          putParcelable(WebViewFragment.CONFIG, FunnelConfig(enableExitBar = false))
        }

    return WebViewFragment().apply { this.arguments = arguments }
  }
}
