package com.ixigo.sdk.webview

import android.os.Bundle
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.*
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.test.initializePaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.Loaded
import com.ixigo.sdk.ui.Loading
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class IxiWebViewTests {

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: ShadowWebView
  private lateinit var fragmentActivity: FragmentActivity
  private lateinit var fragment: WebViewFragment
  private val analyticsProvider = mock<AnalyticsProvider>()

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val paymentInputAdapter by lazy { moshi.adapter(PaymentInput::class.java) }
  private lateinit var ixiWebView: IxiWebView

  private lateinit var ssoAuthProvider: SSOAuthProvider
  private lateinit var paymentProvider: PaymentProvider
  private lateinit var mockViewModel: WebViewViewModel

  @Before
  fun setup() {
    ssoAuthProvider = mock()
    paymentProvider = mock()
    mockViewModel = mock()

    initializeTestIxigoSDK(analyticsProvider = analyticsProvider)
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)

    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      shadowWebView = Shadows.shadowOf(it.webView)
      shadowWebView.pushEntryToHistory(initialPageData.url)
      fragmentActivity = it.requireActivity()
      ixiWebView = IxiWebView(it, ssoAuthProvider, analyticsProvider, mockViewModel)
    }
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test successful Login`() {
    testLogin("token")
  }

  @Test
  fun `test unsuccessful Login`() {
    testLogin(null)
  }

  @Test
  fun `test login returns false if SSOAuthProvider returns false`() {
    whenever(ssoAuthProvider.login(any(), any(), any())).thenReturn(false)
    val ret = ixiWebView.loginUser("", "")
    assertFalse(ret)
  }

  @Test
  fun `test quit`() {
    val delegate: WebViewDelegate = mock()
    fragment.delegate = delegate
    val quitMethod = ixiWebView.javaClass.getDeclaredMethod("quit")
    assertNotNull(quitMethod.getAnnotation(JavascriptInterface::class.java))
    ixiWebView.quit()
    verify(delegate).onQuit()
  }

  @Test
  fun `test successful payment`() {
    Mockito.`when`(mockViewModel.startNativePayment(any(), any())).thenReturn(true)
    val nextUrl = "nextUrl"
    val paymentInputStr =
        """
            |{
            |   "product":"flights",
            |   "data":{
            |       "paymentId":"186CWHA8NAOIHJK1EHX",
            |       "tripId":"IF21102519356877",
            |       "providerId":1044
            |   }
            |}""".trimMargin()
    val paymentInput = paymentInputAdapter.fromJson(paymentInputStr)!!
    val startNativePaymentMethod =
        ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
    val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    assertTrue(paymentReturn)
    assertNotNull(startNativePaymentMethod.getAnnotation(JavascriptInterface::class.java))
  }

  @Test
  fun `test failed payment`() {
    Mockito.`when`(mockViewModel.startNativePayment(any(), any())).thenReturn(false)
    val paymentInputStr =
        """
            |{
            |   "product":"flights",
            |   "data":{
            |       "paymentId":"186CWHA8NAOIHJK1EHX",
            |       "tripId":"IF21102519356877",
            |       "providerId":1044
            |   }
            |}""".trimMargin()
    val paymentInput = paymentInputAdapter.fromJson(paymentInputStr)!!
    val startNativePaymentMethod =
        ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
    val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
    shadowOf(Looper.getMainLooper()).idle()

    Mockito.verify(mockViewModel).startNativePayment(any(), eq(paymentInput))
    assertFalse(paymentReturn)
    assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
  }

  @Test
  fun `test invalid payment`() {
    val paymentInputStr =
        """
            |{
            |   "yolo":"flights"
            |}""".trimMargin()
    val startNativePaymentMethod =
        ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
    val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(paymentReturn)
    assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
  }

  @Test
  fun `test openWindow starts a new WebViewActivity`() {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn appInfo
      on { partnerTokenProvider } doReturn EmptyPartnerTokenProvider
    }
    IxigoSDK.replaceInstance(mockIxigoSDK)
    scenario.onFragment { fragment ->
      val openWindowMethod =
          ixiWebView.javaClass.getDeclaredMethod(
              "openWindow", String::class.java, String::class.java)
      assertNotNull(openWindowMethod.getAnnotation(JavascriptInterface::class.java))
      val url = "openWindowUrl"
      openWindowMethod.invoke(ixiWebView, url, "title")

      verify(mockIxigoSDK, times(1)).launchWebActivity(fragment.requireActivity(), url)
    }
  }

  @Test
  fun `test trackEvent logs event with analyticsProvider`() {
    scenario.onFragment {
      val trackEventMethod =
          ixiWebView.javaClass.getDeclaredMethod(
              "trackEvent", String::class.java, String::class.java)
      assertNotNull(trackEventMethod.getAnnotation(JavascriptInterface::class.java))
      ixiWebView.trackEvent(
          "eventNameValue", """{"prop1": "prop1Value", "prop2": 2, "prop3": false}""")

      verify(analyticsProvider)
          .logEvent(
              Event(
                  "eventNameValue",
                  properties = mapOf("prop1" to "prop1Value", "prop2" to "2.0", "prop3" to "false"),
                  referrer = initialPageData.url))
    }
  }

  @Test
  fun `test trackEvent logs event with analyticsProvider with null properties`() {
    scenario.onFragment {
      ixiWebView.trackEvent("eventNameValue", null)

      verify(analyticsProvider)
          .logEvent(Event("eventNameValue", properties = mapOf(), referrer = initialPageData.url))
    }
  }

  @Test
  fun `test trackEvent logs event with analyticsServiceName`() {
    scenario.onFragment {
      val trackEventMethod =
          ixiWebView.javaClass.getDeclaredMethod(
              "trackEvent", String::class.java, String::class.java, String::class.java)
      assertNotNull(trackEventMethod.getAnnotation(JavascriptInterface::class.java))
      ixiWebView.trackEvent(
          analyticsServiceName = "analyticsServiceNameValue",
          eventName = "eventNameValue",
          """{"prop1": "prop1Value"}""")

      verify(analyticsProvider)
          .logEvent(
              Event(
                  "eventNameValue",
                  properties =
                      mapOf(
                          "prop1" to "prop1Value",
                          "analyticsServiceName" to "analyticsServiceNameValue"),
                  referrer = initialPageData.url))
    }
  }

  @Test
  fun `test pwaReady`() {
    scenario.onFragment {
      assertEquals(Loading(referrer = initialPageData.url), fragment.loadableView.status)
      ixiWebView.pwaReady()
      assertEquals(Loaded, fragment.loadableView.status)
    }
  }

  @Test
  fun `fragment state change is dispatched to on page state change js callbacks`() {
    scenario.onFragment {
      val onPageStateChangeJsFunction = "javascript:alert('STATE')"
      assertNull(shadowWebView.lastEvaluatedJavascript)

      // test register
      ixiWebView.registerPageStateChange(onPageStateChangeJsFunction)

      scenario.moveToState(Lifecycle.State.STARTED)
      assertEquals("javascript:alert('PAUSED')", shadowWebView.lastEvaluatedJavascript)

      scenario.moveToState(Lifecycle.State.RESUMED)
      assertEquals("javascript:alert('RESUMED')", shadowWebView.lastEvaluatedJavascript)

      // test unregister
      ixiWebView.unregisterPageStateChange(onPageStateChangeJsFunction)
      scenario.moveToState(Lifecycle.State.STARTED)
      assertEquals("javascript:alert('RESUMED')", shadowWebView.lastEvaluatedJavascript)
    }
  }

  private fun testLogin(token: String?) {
    doAnswer {
          val callback: AuthCallback = it.getArgument(2)
          if (token == null) {
            callback(Err(Error()))
          } else {
            callback(Ok(AuthData(token)))
          }
          true
        }
        .`when`(ssoAuthProvider)
        .login(eq(fragmentActivity), eq("iximaad"), any())

    val successJs = "success"
    val failureJs = "failure"

    val loginUserMethod =
        ixiWebView.javaClass.getDeclaredMethod("loginUser", String::class.java, String::class.java)
    val loginReturn = loginUserMethod.invoke(ixiWebView, successJs, failureJs) as Boolean
    shadowOf(Looper.getMainLooper()).idle()
    val expectedUrl = if (token == null) failureJs else successJs
    assertTrue(loginReturn)
    assertEquals(expectedUrl, shadowWebView.lastLoadedUrl)
    assertNotNull(loginUserMethod.getAnnotation(JavascriptInterface::class.java))
  }

  private val appInfo = FakeAppInfo
}
