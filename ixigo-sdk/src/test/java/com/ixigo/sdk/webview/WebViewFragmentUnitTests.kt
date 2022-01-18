package com.ixigo.sdk.webview

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
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
import org.mockito.kotlin.mock
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

  @Before
  fun setup() {
    initializeTestIxigoSDK()
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
  fun `test that initial loadingView status is Loaded`() {
    assertEquals(Loaded, fragment.loadableView.status)
  }

  @Test
  fun `test that loadingView status is updated for successful page load`() {
    shadowWebView.webViewClient.onPageStarted(fragment.webView, initialPageData.url, null)
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is updated when an error happens`() {
    shadowWebView.webViewClient.onPageStarted(fragment.webView, initialPageData.url, null)
    assertLoadableViewStatus(Loading())
    shadowWebView.webViewClient.onReceivedError(fragment.webView, mock(), mock())
    assertLoadableViewStatus(Failed())
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    assertLoadableViewStatus(Failed())
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
