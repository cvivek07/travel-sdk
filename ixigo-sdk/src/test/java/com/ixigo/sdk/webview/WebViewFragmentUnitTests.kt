package com.ixigo.sdk.webview

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.Failed
import com.ixigo.sdk.ui.Loaded
import com.ixigo.sdk.ui.Loading
import com.ixigo.sdk.ui.Status
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class WebViewFragmentUnitTests {

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: ShadowWebView
  private lateinit var fragmentActivity: Activity
  private lateinit var fragment: WebViewFragment
  private lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Before
  fun setup() {
    mockAnalyticsProvider = mock()
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      shadowWebView = shadowOf(it.webView)
      shadowWebView.performSuccessfulPageLoadClientCallbacks()
      shadowWebView.pushEntryToHistory(initialPageData.url)
      fragmentActivity = it.requireActivity()
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
  fun `test that IxiWebView is loaded`() {
    val ixiWebView = shadowWebView.getJavascriptInterface("IxiWebView") as IxiWebView
    assertNotNull(ixiWebView)
  }

  @Test
  fun `test that HtmlOut is loaded`() {
    val htmlOut = shadowWebView.getJavascriptInterface("HTMLOUT") as HtmlOutJsInterface
    assertNotNull(htmlOut)
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
  fun `test that error sends an analytics event`() {
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { toString() } doReturn "errorMessage" })
    verify(mockAnalyticsProvider)
        .logEvent(Event.with(action = "webviewError", label = "errorMessage"))
  }

  @Test
  fun `test that http error sends an analytics event`() {
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { statusCode } doReturn 404 })
    verify(mockAnalyticsProvider).logEvent(Event.with(action = "webviewError", label = "404"))
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
    assertEquals(0, shadowWebView.reloadInvocations)
    fragment.loadableView.onRetry?.invoke()
    assertEquals(1, shadowWebView.reloadInvocations)
  }

  private fun assertLoadableViewStatus(status: Status) {
    assertEquals(status, fragment.loadableView.status)
  }
}
