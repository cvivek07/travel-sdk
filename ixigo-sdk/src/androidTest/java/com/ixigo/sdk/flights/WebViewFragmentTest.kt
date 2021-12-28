package com.ixigo.sdk.flights

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.DisabledPaymentProvider
import com.ixigo.sdk.payment.FakePaymentProvider
import com.ixigo.sdk.payment.PaymentResponse
import com.ixigo.sdk.test.util.FileDispatcher
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
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
  private val analyticsProvider = mock<AnalyticsProvider>()

  @Before
  fun setup() {
    mockServer = MockWebServer()
    mockServer.dispatcher = FileDispatcher()

    scenario =
        launchFragmentInContainer(
            Bundle().also {
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
  fun testSuccessfulPayment() {
    val successfulPaymentUrl = "https://www.ixigo.com/"
    IxigoSDK.init(
        InstrumentationRegistry.getInstrumentation().targetContext,
        EmptyAuthProvider,
        FakePaymentProvider(Ok(PaymentResponse(successfulPaymentUrl))),
        appInfo,
        analyticsProvider)
    onWebView().withElement(findElement(Locator.ID, "native_payment_button")).perform(webClick())
    onWebView().check(webMatches(getCurrentUrl(), equalTo(successfulPaymentUrl)))
    assertPaymentEvents(true)
  }

  @Test
  fun testFailedPayment() {
    IxigoSDK.init(
        InstrumentationRegistry.getInstrumentation().targetContext,
        EmptyAuthProvider,
        FakePaymentProvider(Err(Error())),
        appInfo,
        analyticsProvider)
    onWebView().withElement(findElement(Locator.ID, "native_payment_button")).perform(webClick())

    assertElementText("payment_result", "true")
    assertPaymentEvents(false)
  }

  @Test
  fun testNoPaymentAvailable() {
    IxigoSDK.init(
        InstrumentationRegistry.getInstrumentation().targetContext,
        EmptyAuthProvider,
        DisabledPaymentProvider,
        appInfo,
        analyticsProvider)
    onWebView().withElement(findElement(Locator.ID, "native_payment_button")).perform(webClick())

    assertElementText("payment_result", "false")
    assertPaymentEvents(false)
  }

  @Test
  fun testWrongPaymentIdProvided() {
    IxigoSDK.init(
        InstrumentationRegistry.getInstrumentation().targetContext,
        EmptyAuthProvider,
        FakePaymentProvider(Ok(PaymentResponse("https://www.ixigo.com"))),
        appInfo,
        analyticsProvider)
    onWebView()
        .withElement(findElement(Locator.ID, "null_paymentId_native_payment_button"))
        .perform(webClick())

    assertElementText("payment_result", "false")
    assertPaymentEvents(false)
  }

  @Test
  fun testQuit() {
    scenario.onFragment { fragment -> fragment.delegate = fragmentDelegate }

    onWebView().withElement(findElement(Locator.ID, "quit_button")).perform(webClick())

    verify(fragmentDelegate).quit()
  }

  private fun assertPaymentEvents(success: Boolean) {
    verify(analyticsProvider).logEvent(Event(action = "paymentStart"))
    val finishedEventLabel = if (success) "Success" else "Error"
    verify(analyticsProvider)
        .logEvent(Event(action = "paymentFinished", label = finishedEventLabel))
  }

  private fun assertElementText(id: String, expectedText: String) {
    onWebView()
        .withElement(findElement(Locator.ID, id))
        .check(webMatches(getText(), equalTo(expectedText)))
  }

  private fun testLogin(token: String?) {
    IxigoSDK.init(
        InstrumentationRegistry.getInstrumentation().targetContext,
        FakeAuthProvider(token),
        DisabledPaymentProvider,
        appInfo,
        analyticsProvider)
    onWebView().withElement(findElement(Locator.ID, "login_button")).perform(webClick())

    val expectedLoginResult = token ?: "fail"

    onWebView()
        .withElement(findElement(Locator.ID, "login_result"))
        .check(webMatches(getText(), equalTo(expectedLoginResult)))

    verify(analyticsProvider).logEvent(Event(action = "loginStart"))
    val finishedEventLabel = if (token == null) "Error" else "Success"
    verify(analyticsProvider).logEvent(Event(action = "loginFinished", label = finishedEventLabel))
  }

  private val appInfo =
      AppInfo(
          clientId = "clientId",
          apiKey = "apiKey",
          appVersion = "appVersion",
          deviceId = "deviceId",
          uuid = "uuid")
}
