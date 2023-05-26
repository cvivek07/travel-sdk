package com.ixigo.sdk.flights

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.FunnelConfig
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import com.ixigo.sdk.webview.WebViewFragment.Companion.INITIAL_PAGE_DATA_ARGS
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class FlightsFunnelTest {

  private lateinit var scenario: ActivityScenario<FragmentActivity>
  private lateinit var activity: Activity
  private val mockAnalyticsProvider = mock<AnalyticsProvider>()
  private val config = Config("https://baseUrl.ixigo.com/")

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    IxigoSDK.clearInstance()
  }

  @After
  fun tearDown() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test flightsStartHome launches WebActivity with auth header when auth is available`() {
    initializeTestIxigoSDK(
        appInfo = appInfo,
        analyticsProvider = mockAnalyticsProvider,
        config = config,
        authProvider = FakeAuthProvider(token = "token"))
    assertFlightsHome(expectedHeaders = expectedHeaders() + mapOf("Authorization" to "token"))
  }

  @Test
  fun `test flightsStartHome launches WebActivity without auth header when auth is not available`() {
    initializeTestIxigoSDK(
        appInfo = appInfo,
        analyticsProvider = mockAnalyticsProvider,
        config = config,
        authProvider = FakeAuthProvider(token = null))
    assertFlightsHome(expectedHeaders = expectedHeaders())
  }

  @Test
  fun `test flightsStartSearch launches WebActivity with no return Date`() {
    assertFlightSearch(
        searchData =
            FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDate = LocalDate.of(2021, 10, 22),
                source = "FlightSearchFormFragment",
                flightClass = "e",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)),
        expectedUrl =
            "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=&adults=1&children=0&infants=0&class=e&source=FlightSearchFormFragment")
  }

  @Test
  fun `test flightsStartSearch launches WebActivity with return Date`() {
    assertFlightSearch(
        searchData =
            FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDate = LocalDate.of(2021, 10, 22),
                returnDate = LocalDate.of(2021, 10, 26),
                source = "FlightSearchFormFragment",
                flightClass = "a",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)),
        expectedUrl =
            "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=26102021&adults=1&children=0&infants=0&class=a&source=FlightSearchFormFragment")
  }

  @Test
  fun `test flightsStartSearch launches WebActivity with dates as Strings`() {
    assertFlightSearch(
        searchData =
            FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDateStr = "22102021",
                returnDateStr = "26102021",
                source = "FlightSearchFormFragment",
                flightClass = "e",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)),
        expectedUrl =
            "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=26102021&adults=1&children=0&infants=0&class=e&source=FlightSearchFormFragment")
  }

  @Test
  fun `test flightsStartSearch launches WebActivity with wrong String Dates`() {
    val tomorrowStr = formatDate(LocalDate.now().plusDays(1))
    assertFlightSearch(
        searchData =
            FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDateStr = "aasdfds",
                returnDateStr = "asdfd",
                source = "FlightSearchFormFragment",
                flightClass = "e",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)),
        expectedUrl =
            "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=${tomorrowStr}&returnDate=&adults=1&children=0&infants=0&class=e&source=FlightSearchFormFragment")
  }

  @Test
  fun `test flightMultiModelFragment returns WebViewFragment`() {
    initializeTestIxigoSDK(
        appInfo = appInfo, analyticsProvider = mockAnalyticsProvider, config = config)
    val searchData =
        FlightSearchData(
            origin = "DEL",
            destination = "BOM",
            departDateStr = "22102021",
            returnDateStr = "26102021",
            source = "FlightSearchFormFragment",
            flightClass = "e",
            passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0))
    val fragment = IxigoSDK.instance.flightsMultiModelFragment(searchData)
    assertNotNull(fragment as? WebViewFragment)

    val url =
        "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_LISTING_MULTI_MODEL&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=26102021&adults=1&children=0&infants=0&class=e&source=FlightSearchFormFragment"
    val expectedInitialData = InitialPageData(url, expectedHeaders())
    assertEquals(expectedInitialData, fragment.arguments!!.getParcelable(INITIAL_PAGE_DATA_ARGS))

    val expectedConfig = FunnelConfig(enableExitBar = false)
    assertEquals(expectedConfig, fragment.arguments!!.getParcelable(WebViewFragment.CONFIG))
  }

  @Test
  fun `test flightsStartTrips launches WebActivity`() {
    initializeTestIxigoSDK(
        appInfo = appInfo, analyticsProvider = mockAnalyticsProvider, config = config)
    scenario.onActivity { activity ->
      IxigoSDK.instance.flightsStartTrips(activity)
      assertLaunchedIntent(
          activity,
          "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_TRIPS",
          expectedHeaders = expectedHeaders())
      verify(mockAnalyticsProvider).logEvent(Event.with(action = "flightsStartTrips"))
    }
  }

  @Test
  fun `test flightTripsFragment returns WebViewFragment with correct URL`() {
    initializeTestIxigoSDK(
        appInfo = appInfo, analyticsProvider = mockAnalyticsProvider, config = config)
    val fragment = IxigoSDK.instance.flightsTripsFragment()
    assertNotNull(fragment as? WebViewFragment)

    val url =
        "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_TRIPS&displayMode=embedded"
    val expectedInitialData = InitialPageData(url, expectedHeaders())
    assertEquals(expectedInitialData, fragment.arguments!!.getParcelable(INITIAL_PAGE_DATA_ARGS))

    val expectedConfig = FunnelConfig(enableExitBar = false)
    assertEquals(expectedConfig, fragment.arguments!!.getParcelable(WebViewFragment.CONFIG))
  }

  private fun assertFlightsHome(expectedHeaders: Map<String, String> = expectedHeaders()) {
    scenario.onActivity { activity ->
      IxigoSDK.instance.flightsStartHome(activity)
      assertLaunchedIntent(
          activity,
          "https://baseUrl.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=FLIGHT_HOME",
          expectedHeaders = expectedHeaders)
      verify(mockAnalyticsProvider).logEvent(Event.with(action = "flightsStartHome"))
    }
  }

  private fun assertFlightSearch(searchData: FlightSearchData, expectedUrl: String) {
    initializeTestIxigoSDK(
        appInfo = appInfo, analyticsProvider = mockAnalyticsProvider, config = config)
    scenario.onActivity { activity ->
      IxigoSDK.instance.flightsStartSearch(activity, searchData)
      assertLaunchedIntent(activity, expectedUrl, expectedHeaders = expectedHeaders())
      verify(mockAnalyticsProvider).logEvent(Event.with(action = "flightsStartSearch"))
    }
  }

  private val appInfo =
      AppInfo(
          clientId = "clientId",
          apiKey = "apiKey",
          appVersion = 1,
          appName = "appName",
          deviceId = "deviceId",
          uuid = "uuid")

  private fun expectedHeaders(): Map<String, String> =
      mutableMapOf(
          "appVersion" to appInfo.appVersionString,
          "clientId" to appInfo.clientId,
          "apiKey" to appInfo.apiKey,
          "deviceId" to appInfo.deviceId,
          "uuid" to appInfo.uuid,
      )
}
