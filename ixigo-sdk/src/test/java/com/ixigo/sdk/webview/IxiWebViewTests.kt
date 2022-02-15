package com.ixigo.sdk.webview

import android.os.Bundle
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.*
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

  @Before
  fun setup() {
    ssoAuthProvider = mock()
    paymentProvider = mock()

    IxigoSDK.replaceInstance(
        IxigoSDK(appInfo, EmptyPartnerTokenProvider, DisabledPaymentProvider, analyticsProvider))

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
      ixiWebView = IxiWebView(it, ssoAuthProvider)
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
    IxigoSDK.replaceInstance(
        IxigoSDK(
            appInfo,
            EmptyPartnerTokenProvider,
            FakePaymentProvider(
                fragmentActivity, mapOf(paymentInput to Ok(PaymentResponse(nextUrl)))),
            analyticsProvider))
    val startNativePaymentMethod =
        ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
    val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    assertTrue(paymentReturn)
    assertEquals(nextUrl, shadowWebView.lastLoadedUrl)
    assertNotNull(startNativePaymentMethod.getAnnotation(JavascriptInterface::class.java))
  }

  @Test
  fun `test failed payment`() {
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
    IxigoSDK.replaceInstance(
        IxigoSDK(
            appInfo,
            EmptyPartnerTokenProvider,
            FakePaymentProvider(fragmentActivity, mapOf(paymentInput to Err(Error()))),
            analyticsProvider))
    val startNativePaymentMethod =
        ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
    val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    assertTrue(paymentReturn)
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
