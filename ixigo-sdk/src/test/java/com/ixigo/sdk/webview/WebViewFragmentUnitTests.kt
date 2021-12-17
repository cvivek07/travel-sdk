package com.ixigo.sdk.webview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class WebViewFragmentUnitTests {

    private lateinit var scenario: FragmentScenario<WebViewFragment>
    private val fragmentDelegate = mock<WebViewFragmentDelegate>()
    private val initialPageData =
        InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
    private lateinit var shadowWebView: ShadowWebView
    private lateinit var fragmentActivity: Activity
    private val analyticsProvider = mock<AnalyticsProvider>()

    private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    private val paymentInputAdapter by lazy { moshi.adapter(PaymentInput::class.java) }

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(Bundle().also {
            it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
        })
        scenario.onFragment {
            shadowWebView = shadowOf(it.webView)
            shadowWebView.pushEntryToHistory(initialPageData.url)
            it.delegate = fragmentDelegate
            fragmentActivity = it.requireActivity()
        }
    }

    @After
    fun teardown() {
        IxigoSDK.clearInstance()
    }

    @Test
    fun `test that initial Url is loaded`() {
        assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
        assertEquals(initialPageData.headers, shadowWebView.lastAdditionalHttpHeaders)
    }

    @Test
    fun `test that initial Url is not loaded if not present`() {
        scenario = launchFragmentInContainer()
        scenario.onFragment {
            val shadowWebView = shadowOf(it.webView)
            assertNull(shadowWebView.lastLoadedUrl)
            assertNull(shadowWebView.lastAdditionalHttpHeaders)
        }
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
        val quitMethod = ixiWebView.javaClass.getDeclaredMethod("quit")
        quitMethod.invoke(ixiWebView)
        verify(fragmentDelegate).quit()
    }

    @Test
    fun `test successful payment`() {
        val nextUrl = "nextUrl"
        val paymentInputStr = """
            |{
            |   "product":"flights",
            |   "data":{
            |       "paymentId":"186CWHA8NAOIHJK1EHX",
            |       "tripId":"IF21102519356877",
            |       "providerId":1044
            |   }
            |}""".trimMargin()
        val paymentInput = paymentInputAdapter.fromJson(paymentInputStr)!!
        IxigoSDK.init(
            fragmentActivity,
            EmptyAuthProvider,
            FakePaymentProvider(
                fragmentActivity,
                mapOf(paymentInput to Ok(PaymentResponse(nextUrl)))
            ),
            appInfo,
            analyticsProvider
        )
        val startNativePaymentMethod =
            ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
        shadowOf(getMainLooper()).idle()

        assertTrue(paymentReturn)
        assertEquals(nextUrl, shadowWebView.lastLoadedUrl)
        assertNotNull(startNativePaymentMethod.getAnnotation(JavascriptInterface::class.java))
    }

    @Test
    fun `test failed payment`() {
        val paymentInputStr = """
            |{
            |   "product":"flights",
            |   "data":{
            |       "paymentId":"186CWHA8NAOIHJK1EHX",
            |       "tripId":"IF21102519356877",
            |       "providerId":1044
            |   }
            |}""".trimMargin()
        val paymentInput = paymentInputAdapter.fromJson(paymentInputStr)!!
        IxigoSDK.init(
            fragmentActivity,
            EmptyAuthProvider,
            FakePaymentProvider(fragmentActivity, mapOf(paymentInput to Err(Error()))),
            appInfo,
            analyticsProvider
        )
        val startNativePaymentMethod =
            ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
        shadowOf(getMainLooper()).idle()

        assertTrue(paymentReturn)
        assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    }

    @Test
    fun `test invalid payment`() {
        IxigoSDK.init(
            fragmentActivity,
            EmptyAuthProvider,
            FakePaymentProvider(fragmentActivity, mapOf()),
            appInfo,
            analyticsProvider
        )
        val paymentInputStr = """
            |{
            |   "yolo":"flights"
            |}""".trimMargin()
        val startNativePaymentMethod =
            ixiWebView.javaClass.getDeclaredMethod("executeNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentInputStr) as Boolean
        shadowOf(getMainLooper()).idle()

        assertFalse(paymentReturn)
        assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    }

    @Test
    fun `test activity result is forwarded to paymentProvider`() {
        val requestCode = 123
        val responseCode = 456
        val intent = Intent()
        val paymentProvider = mock<ActivityResultPaymentProvider>()

        IxigoSDK.init(
            fragmentActivity,
            EmptyAuthProvider,
            paymentProvider,
            appInfo,
            analyticsProvider
        )

        scenario.onFragment { fragment ->
            fragment.onActivityResult(requestCode, responseCode, intent)
            verify(paymentProvider).handle(requestCode, responseCode, intent)
        }
    }

    @Test
    fun `test backButton goes back if Webview can go back`() {
        shadowWebView.pushEntryToHistory("https://www.ixigo.com/page1")
        fragmentActivity.onBackPressed()
        assertEquals(1, shadowWebView.goBackInvocations)
    }

    @Test
    fun `test backButton calls quit if Webview can not go back`() {
        fragmentActivity.onBackPressed()
        assertEquals(0, shadowWebView.goBackInvocations)
        verify(fragmentDelegate).quit()
    }

    @Test
    fun `test openWindow starts a new WebViewActivity`() {
        val mockIxigoSDK:IxigoSDK = mock {
            on {appInfo} doReturn appInfo
            on {authProvider} doReturn EmptyAuthProvider
        }
        IxigoSDK.replaceInstance(mockIxigoSDK)
        scenario.onFragment { fragment ->
            val openWindowMethod = ixiWebView.javaClass.getDeclaredMethod(
                "openWindow",
                String::class.java,
                String::class.java
            )
            val url = "openWindowUrl"
            openWindowMethod.invoke(ixiWebView, url, "title")

            verify(mockIxigoSDK, times(1)).launchWebActivity(fragment.requireActivity(), url)
        }
    }

    private fun testLogin(token: String?) {
        IxigoSDK.init(
            fragmentActivity,
            FakeAuthProvider(token),
            EmptyPaymentProvider,
            appInfo,
            analyticsProvider
        )
        val successJs = "success"
        val failureJs = "failure"

        val loginUserMethod = ixiWebView.javaClass.getDeclaredMethod(
            "loginUser",
            String::class.java,
            String::class.java
        )
        val loginReturn = loginUserMethod.invoke(ixiWebView, successJs, failureJs) as Boolean
        shadowOf(getMainLooper()).idle()
        val expectedUrl = if (token == null) failureJs else successJs
        assertTrue(loginReturn)
        assertEquals(expectedUrl, shadowWebView.lastLoadedUrl)
        assertNotNull(loginUserMethod.getAnnotation(JavascriptInterface::class.java))
    }

    private val appInfo = AppInfo(
        clientId = "clientId",
        apiKey = "apiKey",
        appVersion = "appVersion",
        deviceId = "deviceId",
        uuid = "uuid"
    )

    private val ixiWebView by lazy { shadowWebView.getJavascriptInterface("IxiWebView") }
}

private class FakePaymentProvider(
    private val expectedActivity: Activity,
    private val results: Map<PaymentInput, PaymentResult>
) : PaymentProvider {
    override fun startPayment(
        activity: FragmentActivity,
        input: PaymentInput,
        callback: PaymentCallback
    ): Boolean {
        assertSame(expectedActivity, activity)
        val result = results[input] ?: return false
        callback(result)
        return true
    }
}

private interface ActivityResultPaymentProvider : PaymentProvider, ActivityResultHandler