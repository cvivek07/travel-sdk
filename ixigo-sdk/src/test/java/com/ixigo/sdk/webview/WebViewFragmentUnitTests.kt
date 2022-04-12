package com.ixigo.sdk.webview

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.DeeplinkHandler
import com.ixigo.sdk.Handled
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.test.ActivityResultPartnerTokenProvider
import com.ixigo.sdk.payment.ActivityResultPaymentProvider
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.Failed
import com.ixigo.sdk.ui.Loaded
import com.ixigo.sdk.ui.Loading
import com.ixigo.sdk.ui.Status
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
@Config(shadows = [CustomShadowWebview::class])
class WebViewFragmentUnitTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: CustomShadowWebview
  private lateinit var shadowActivity: ShadowActivity
  private lateinit var fragmentActivity: Activity
  private lateinit var fragment: WebViewFragment
  private lateinit var delegate: WebViewDelegate

  @Mock private lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Mock private lateinit var mockDeeplinkHandler: DeeplinkHandler

  @Before
  fun setup() {
    initializeTestIxigoSDK(
        analyticsProvider = mockAnalyticsProvider, deeplinkHandler = mockDeeplinkHandler)
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    delegate = mock()
    scenario.onFragment {
      fragment = it
      fragment.delegate = delegate
      shadowWebView = shadowOf(it.webView) as CustomShadowWebview
      shadowWebView.performSuccessfulPageLoadClientCallbacks()
      shadowWebView.pushEntryToHistory(initialPageData.url)
      fragmentActivity = it.requireActivity()
      shadowActivity = shadowOf(fragmentActivity)
    }
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test that webview is correctly configured`() {
    with(fragment.webView.settings) {
      assertTrue(domStorageEnabled)
      assertTrue(javaScriptEnabled)
    }
  }

  @Test
  fun `test that initial Url is loaded`() {
    assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    assertEquals(initialPageData.headers, shadowWebView.lastAdditionalHttpHeaders)
  }

  interface JsInterfaceAndListener : JsInterface, WebViewFragmentListener

  @Test
  fun `test that jsInterfaces are loaded`() {
    val mockJsInterface: JsInterface = mock { on { name } doReturn "mockJsInterface" }
    val mockJsInterfaceListener: JsInterfaceAndListener = mock {
      on { name } doReturn "mockJsInterfaceListener"
    }
    IxigoSDK.instance.webViewConfig.addJsInterfaceProvider(
        object : JsInterfaceProvider {
          override fun getJsInterfaces(
              url: String,
              webViewFragment: WebViewFragment
          ): List<JsInterface> {
            return listOf(mockJsInterface, mockJsInterfaceListener)
          }
        })
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      shadowWebView = shadowOf(it.webView) as CustomShadowWebview
      assertEquals(mockJsInterface, shadowWebView.getJavascriptInterface("mockJsInterface"))
      assertEquals(
          mockJsInterfaceListener, shadowWebView.getJavascriptInterface("mockJsInterfaceListener"))

      val nextUrl = "https://www.ixigo.com/next-page"
      shadowWebView.webViewClient.onPageStarted(fragment.webView, nextUrl, null)
      verify(mockJsInterfaceListener).onUrlLoadStart(it, nextUrl)
    }
  }

  @Test
  fun `test that back button navigates back in WebView when possible`() {
    val url = "https://www.ixigo.com/page2"
    shadowWebView.pushEntryToHistory(url)
    shadowWebView.webViewClient.doUpdateVisitedHistory(fragment.webView, url, false)
    assertEquals(0, shadowWebView.goBackInvocations)
    fragmentActivity.onBackPressed()
    assertEquals(1, shadowWebView.goBackInvocations)
  }

  @Test
  fun `test that back button finishes activity if WebView can not go back`() {
    fragmentActivity.onBackPressed()
    assertEquals(0, shadowWebView.goBackInvocations)
    assert(fragmentActivity.isFinishing)
  }

  @Test
  fun `test that initial loadingView status is Loading`() {
    assertEquals(Loading(), fragment.loadableView.status)
  }

  @Test
  fun `test that status is Loading after loading new url`() {
    assertEquals(Loading(), fragment.loadableView.status)
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse("https://www.ixigo.com/page2") })
    assertLoadableViewStatus(Loading())
    verify(mockAnalyticsProvider)
        .logEvent(Event.with(action = "webviewStartLoad", referrer = "https://www.ixigo.com/page2"))
  }

  @Test
  fun `test that loadingView status is updated for successful page load`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is updated when an error happens`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Failed())
  }

  @Test
  fun `test that loadingView status is updated when an http error happens`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Failed())
  }

  @Test
  fun `test that http error is ignored if it is not the loading url`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView, mock { on { url } doReturn Uri.parse("https://random.com") }, mock())
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is not set to error when an error happens but we were not loading a page`() {
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
    shadowWebView.webViewClient.onReceivedError(fragment.webView, mock(), mock())
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is not set to error when an we received an unknown Error`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { errorCode } doReturn WebViewClient.ERROR_UNKNOWN })
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that error sends an analytics event`() {
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { toString() } doReturn "errorMessage" })
    verify(mockAnalyticsProvider)
        .logEvent(
            Event.with(
                action = "webviewError",
                label = "errorMessage",
                referrer = "https://www.ixigo.com"))
  }

  @Test
  fun `test that http error sends an analytics event`() {
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { statusCode } doReturn 404 })
    verify(mockAnalyticsProvider)
        .logEvent(
            Event.with(action = "webviewError", label = "404", referrer = "https://www.ixigo.com"))
  }

  @Test
  fun `test that goBack finishes Activity when loadingView onGoBack is called and there is no navigation stack`() {
    val url = "https://www.ixigo.com/page2"
    shadowWebView.pushEntryToHistory(url)
    shadowWebView.webViewClient.doUpdateVisitedHistory(fragment.webView, url, false)
    assertEquals(0, shadowWebView.goBackInvocations)
    fragment.loadableView.onGoBack?.invoke()
    assertEquals(1, shadowWebView.goBackInvocations)
  }

  @Test
  fun `test that goBack navigates back when loadingView onGoBack is called and there is navigation stack`() {
    fragment.loadableView.onGoBack?.invoke()
    assert(fragmentActivity.isFinishing)
  }

  @Test
  fun `test that onRetry reloads Webview`() {
    // Simulate error
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Failed())

    // Reload
    assertEquals(0, shadowWebView.reloadInvocations)
    fragment.loadableView.onRetry?.invoke()
    assertEquals(1, shadowWebView.reloadInvocations)
    assertEquals(Loading(), fragment.loadableView.status)
  }

  @Test
  fun `test statusBar color is updated from theme-color`() {
    initializeTestIxigoSDK()
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      fragment.delegate = delegate
      fragmentActivity = it.requireActivity()
      shadowWebView = shadowOf(it.webView) as CustomShadowWebview
      shadowWebView.jsCallbacks["document.querySelector('meta[name=\"theme-color\"]').content"] =
          "#00FF00"
      shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
      verify(delegate).updateStatusBarColor(Color.parseColor("#00FF00"))
    }
  }

  @Test
  fun `test activity result is forwarded to paymentProvider and partnerTokenProvider`() {
    val requestCode = 123
    val responseCode = 456
    val intent = Intent()
    val paymentProvider = mock<ActivityResultPaymentProvider>()
    val partnerTokenProvider = mock<ActivityResultPartnerTokenProvider>()

    initializeTestIxigoSDK(
        paymentProvider = paymentProvider, partnerTokenProvider = partnerTokenProvider)

    scenario.onFragment { fragment ->
      fragment.onActivityResult(requestCode, responseCode, intent)
      verify(paymentProvider).handle(requestCode, responseCode, intent)
      verify(partnerTokenProvider).handle(requestCode, responseCode, intent)
    }
  }

  @Test
  fun `test mailto links are open as an activity`() {
    assertOpensNonNetworkUri("mailto:pepe@ixigo.com")
  }

  @Test
  fun `test tel links are open as an activity`() {
    assertOpensNonNetworkUri("tel:1234567890")
  }

  private fun assertOpensNonNetworkUri(uriString: String) {
    val uri = Uri.parse(uriString)
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView, mock<WebResourceRequest> { on { url } doReturn uri })
    val nextIntent = shadowActivity.nextStartedActivity
    assertEquals(ACTION_VIEW, nextIntent.action)
    assertEquals(uri, nextIntent.data)
  }

  @Test
  fun `test deeplinkHandler can intercept a uri`() {
    val uri = Uri.parse("https://www.ixigo.com/deeplink")
    whenever(mockDeeplinkHandler.handleUri(any(), eq(uri))).thenReturn(Handled)

    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView, mock<WebResourceRequest> { on { url } doReturn uri })
    assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    verify(mockDeeplinkHandler).handleUri(any(), eq(uri))
  }

  @Test
  fun `test Ixigo JS SDK is loaded if needed`() {
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertEquals(
        """
        if (!window.IxigoSDK) {
          var loadIxigoSDK = function() {
              var script = document.createElement("script");
              script.type = "text/javascript";
              script.src = "https://rocket.ixigo.com/ixigo-js-sdk/latest/index.umd.js";
              document.body.appendChild(script);
          }
          if (document.readyState === 'complete') {
              loadIxigoSDK();
          } else {
              window.addEventListener('load', loadIxigoSDK);
          }
        }
      """.trimIndent(),
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test loadUrl loads url in webView`() {
    val url = "https://www.ixigo.com/nextUrl"
    fragment.loadUrl(url)
    assertEquals(url, shadowWebView.lastLoadedUrl)
  }

  private fun assertLoadableViewStatus(status: Status) {
    assertEquals(status, fragment.loadableView.status)
  }
}

@Implements(WebView::class)
class CustomShadowWebview : ShadowWebView() {
  var jsCallbacks: MutableMap<String, String> = mutableMapOf()
  override fun evaluateJavascript(script: String?, callback: ValueCallback<String>?) {
    super.evaluateJavascript(script, callback)
    jsCallbacks[script]?.let { callback?.onReceiveValue(it) }
  }
}
