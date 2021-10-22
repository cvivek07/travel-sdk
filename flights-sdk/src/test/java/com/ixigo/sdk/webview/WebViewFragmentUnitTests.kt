package com.ixigo.sdk.webview

import android.os.Bundle
import android.os.Looper.getMainLooper
import android.webkit.JavascriptInterface
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.payment.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class WebViewFragmentUnitTests {

    private lateinit var scenario: FragmentScenario<WebViewFragment>
    private val fragmentDelegate = mock<WebViewFragmentDelegate>()
    private val initialPageData = InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value") )
    private lateinit var shadowWebView: ShadowWebView

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(Bundle().also {
            it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
        })
        scenario.onFragment {
            shadowWebView = shadowOf(it.webView)
            it.delegate = fragmentDelegate
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
        val paymentId = "paymentId"
        IxigoSDK.init(
            EmptyAuthProvider,
            FakePaymentProvider(mapOf(paymentId to Ok(PaymentResponse(nextUrl)))),
            appInfo
        )
        val startNativePaymentMethod = ixiWebView.javaClass.getDeclaredMethod("startNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentId) as Boolean
        shadowOf(getMainLooper()).idle()

        assertTrue(paymentReturn)
        assertEquals(nextUrl, shadowWebView.lastLoadedUrl)
        assertNotNull(startNativePaymentMethod.getAnnotation(JavascriptInterface::class.java))
    }

    @Test
    fun `test failed payment`() {
        val paymentId = "paymentId"
        IxigoSDK.init(
            EmptyAuthProvider,
            FakePaymentProvider(mapOf(paymentId to Err(Error()))),
            appInfo
        )
        val startNativePaymentMethod = ixiWebView.javaClass.getDeclaredMethod("startNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, paymentId) as Boolean
        shadowOf(getMainLooper()).idle()

        assertTrue(paymentReturn)
        assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    }

    @Test
    fun `test null payment`() {
        IxigoSDK.init(
            EmptyAuthProvider,
            FakePaymentProvider(mapOf()),
            appInfo
        )
        val startNativePaymentMethod = ixiWebView.javaClass.getDeclaredMethod("startNativePayment", String::class.java)
        val paymentReturn = startNativePaymentMethod.invoke(ixiWebView, null) as Boolean
        shadowOf(getMainLooper()).idle()

        assertFalse(paymentReturn)
        assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    }

    private fun testLogin(token: String?) {
        IxigoSDK.init(
            FakeAuthProvider(token),
            EmptyPaymentProvider,
            appInfo
        )
        val successJs = "success"
        val failureJs = "failure"

        val loginUserMethod = ixiWebView.javaClass.getDeclaredMethod("loginUser", String::class.java, String::class.java)
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

private class FakePaymentProvider(private val results: Map<String, PaymentResult>):PaymentProvider {
    override fun startPayment(input: PaymentInput, callback: PaymentCallback): Boolean {
        val result = results[input.paymentId] ?: return false
        callback(result)
        return true
    }
}