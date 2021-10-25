package com.ixigo.sdk.flights

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.payment.EmptyPaymentProvider
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FlightsFunnelTest {

    private lateinit var scenario: ActivityScenario<Activity>

    @Before
    fun setup() {
        scenario = launchActivity()
    }

    @After
    fun tearDown() {
        IxigoSDK.clearInstance()
    }

    @Test
    fun `test flightsStartHome launches WebActivity`() {
        IxigoSDK.init(EmptyAuthProvider, EmptyPaymentProvider, appInfo)
        assertFlightsHome()
    }

    @Test
    fun `test flightsStartHome launches WebActivity with Auth Token`() {
        IxigoSDK.init(FakeAuthProvider("token", AuthData("token")), EmptyPaymentProvider, appInfo)
        assertFlightsHome()
    }

    @Test
    fun `test flightsStartSearch launches WebActivity with no return Date`() {
        assertFlightSearch(
            searchData = FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDate = LocalDate.of(2021,10, 22),
                source = "FlightSearchFormFragment",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)
            ),
            expectedUrl = "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=appVersion&deviceId=deviceId&class=e&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=&adults=1&children=0&infants=0&source=FlightSearchFormFragment"
        )
    }

    @Test
    fun `test flightsStartSearch launches WebActivity with return Date`() {
        assertFlightSearch(
            searchData = FlightSearchData(
                origin = "DEL",
                destination = "BOM",
                departDate = LocalDate.of(2021,10, 22),
                returnDate = LocalDate.of(2021,10, 26),
                source = "FlightSearchFormFragment",
                passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)
            ),
            expectedUrl = "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=appVersion&deviceId=deviceId&class=e&languageCode=en&page=FLIGHT_LISTING&orgn=DEL&dstn=BOM&departDate=22102021&returnDate=26102021&adults=1&children=0&infants=0&source=FlightSearchFormFragment"
        )
    }

    private fun assertFlightsHome() {
        scenario.onActivity { activity ->
            IxigoSDK.getInstance().flightsStartHome(activity)
            assertLaunchedIntent(activity, "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=appVersion&deviceId=deviceId&class=e&languageCode=en&page=FLIGHT_HOME")
        }
    }

    private fun assertFlightSearch(searchData: FlightSearchData, expectedUrl: String) {
        IxigoSDK.init(EmptyAuthProvider, EmptyPaymentProvider, appInfo)
        scenario.onActivity { activity ->
            IxigoSDK.getInstance().flightsStartSearch(activity, searchData)
            assertLaunchedIntent(activity, expectedUrl)
        }
    }

    private fun assertLaunchedIntent(activity: Activity, url: String) {
        val intent = Intent(activity, WebActivity::class.java)
        intent.putExtra(
            WebViewFragment.INITIAL_PAGE_DATA_ARGS,
            InitialPageData(
                url,
                expectedHeaders()
            )
        )
        val shadowActivity = shadowOf(activity)
        val nextIntent = shadowActivity.nextStartedActivity
        assertThat(nextIntent, IntentMatcher(intent))
    }

    private val appInfo = AppInfo(
        clientId = "clientId",
        apiKey = "apiKey",
        appVersion = "appVersion",
        deviceId = "deviceId",
        uuid = "uuid"
    )

    private fun expectedHeaders(): Map<String, String> =
        mutableMapOf(
            "appVersion" to appInfo.appVersion,
            "clientId" to appInfo.clientId,
            "apiKey" to appInfo.apiKey,
            "deviceId" to appInfo.deviceId,
            "uuid" to appInfo.uuid,
        ).also {
            val authData = IxigoSDK.getInstance().authProvider.authData
            if (authData != null) {
                it["Authorization"] = authData.token
            }
        }

    private class IntentMatcher(val intent: Intent) : BaseMatcher<Intent>() {
        override fun describeTo(description: Description?) {
        }

        override fun matches(item: Any?): Boolean {
            val itemIntent = item as Intent? ?: return false
            return itemIntent.filterEquals(intent) && getInitialPageData(itemIntent) == getInitialPageData(
                intent
            )
        }

        private fun getInitialPageData(intent: Intent): InitialPageData =
            intent.getParcelableExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS)!!

    }
}