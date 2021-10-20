package com.ixigo.sdk.flights

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.payment.EmptyPaymentProvider
import com.ixigo.sdk.test.util.FileDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class WebViewFragmentTest {
    private lateinit var mockServer: MockWebServer
    private lateinit var scenario: FragmentScenario<WebViewFragment>
    private val fragmentDelegate = mock<WebViewFragmentDelegate>()

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.dispatcher = FileDispatcher()

        scenario = launchFragmentInContainer(Bundle().also {
            val url = mockServer.url("com/ixigo/sdk/flights/WebViewFragmentTest.html").toString()
            it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url))
        })
    }

    @After
    fun teardown() {
        IxigoSDK.clearInstance()
    }

    @Test
    fun testSuccessfulLogin() {
        testLogin("123456")
    }

    @Test
    fun testFailLogin() {
        testLogin(null)
    }

    @Test
    fun testQuit() {
        scenario.onFragment { fragment ->
            fragment.delegate = fragmentDelegate
        }

        onWebView()
            .withElement(findElement(Locator.ID, "quit_button"))
            .perform(webClick())

        verify(fragmentDelegate).quit()
    }

    private fun testLogin(token: String?) {
        IxigoSDK.init(
            FakeAuthProvider(token),
            EmptyPaymentProvider,
            AppInfo(
                clientId = "clientId",
                apiKey = "apiKey",
                appVersion = "appVersion",
                deviceId = "deviceId",
                uuid = "uuid"
            )
        )
        onWebView()
            .withElement(findElement(Locator.ID, "login_button"))
            .perform(webClick())

        val expectedLoginResult = token ?: "fail"

        onWebView()
            .withElement(findElement(Locator.ID, "login_result")).check(
                webMatches(
                    getText(),
                    equalTo(expectedLoginResult)
                )
            )
    }
}
