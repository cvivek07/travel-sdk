package com.ixigo.sdk.webview

import android.app.Activity
import android.app.Application
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
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.DeeplinkHandler
import com.ixigo.sdk.Handled
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.test.ActivityResultPartnerTokenProvider
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.payment.ActivityResultPaymentProvider
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentResponse
import com.ixigo.sdk.payment.PaymentSDK
import com.ixigo.sdk.test.initializePaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.*
import com.ixigo.sdk.util.AssetFileReader
import com.ixigo.sdk.util.isIxigoUrl
import java.util.*
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
      InitialPageData(
          "https://www.ixigo.com/pwa/initialpage?page=PAYMENT&paymentId=2ME8HW9KHJJS2UNQV&tripId=IXITRS52916728049791&txnId=231QWER89&productType=FLIGHT",
          mapOf("Authorization" to "Bearer token"))
  private lateinit var shadowWebView: CustomShadowWebview
  private lateinit var shadowActivity: ShadowActivity
  private lateinit var fragmentActivity: Activity
  private lateinit var fragment: WebViewFragment
  private lateinit var delegate: WebViewDelegate

  @Mock private lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Mock private lateinit var mockDeeplinkHandler: DeeplinkHandler

  private val javascriptThemeScript =
      """
        eval({solidThemeColor: document.querySelector('meta[name=\"theme-color\"]')?.content,
              gradientThemeColor: document.querySelector('meta[name=\"sdk-theme\"]')?.content})
      """.trimIndent()

  private lateinit var spyPaymentSdk: PaymentSDK

  @Before
  fun setup() {
    initializeTestIxigoSDK(
        analyticsProvider = mockAnalyticsProvider, deeplinkHandler = mockDeeplinkHandler)
    initializePaymentSDK().also { spyPaymentSdk = spy(PaymentSDK.instance) }
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    delegate = mock()
    scenario.onFragment {
      fragment = it
      fragment.paymentSDK = spyPaymentSdk
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
  fun `test fragment quits on payment result`() {
    fragment.viewModel.paymentResult.value =
        NativePaymentResult(
            PaymentInput("flights", emptyMap()),
            Ok(PaymentResponse("https://ixigo.com/payment/complete")))

    verify(delegate).onQuit()
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
  fun `test that initial loadingView status is Loading`() {
    assertEquals(Loading(referrer = initialPageData.url), fragment.loadableView.status)
  }

  @Test
  fun `test that status is Loaded after loading initial url`() {
    assertEquals(Loading(referrer = initialPageData.url), fragment.loadableView.status)
    finishPageLoad()
    assertLoadableViewStatus(Loaded)
    val newPage = "https://www.ixigo.com/page2"
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView, mock<WebResourceRequest> { on { url } doReturn Uri.parse(newPage) })
    assertLoadableViewStatus(Loaded)
    verify(mockAnalyticsProvider)
        .logEvent(Event.with(action = "webviewStartLoad", referrer = newPage))
  }

  @Test
  fun `test that loadingView status is updated for successful page load`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    finishPageLoad()
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is updated when an error happens`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    finishPageLoad()
    assertLoadableViewStatus(Failed())
  }

  @Test
  fun `test that loadingView status is updated when an http error happens`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    finishPageLoad()
    assertLoadableViewStatus(Failed())
  }

  @Test
  fun `test that http error is ignored if it is not the loading url`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView, mock { on { url } doReturn Uri.parse("https://random.com") }, mock())
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    finishPageLoad()
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is not set to error when an error happens but we were not loading a page`() {
    finishPageLoad()
    assertLoadableViewStatus(Loaded)
    shadowWebView.webViewClient.onReceivedError(fragment.webView, mock(), mock())
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test that loadingView status is not set to error when an we received an unknown Error`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    finishPageLoad()
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
                action = "webviewError", label = "errorMessage", referrer = initialPageData.url))
  }

  @Test
  fun `test that http error sends an analytics event`() {
    shadowWebView.webViewClient.onReceivedHttpError(
        fragment.webView,
        mock { on { url } doReturn Uri.parse(initialPageData.url) },
        mock { on { statusCode } doReturn 404 })
    verify(mockAnalyticsProvider)
        .logEvent(
            Event.with(action = "webviewError", label = "404", referrer = initialPageData.url))
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
    verify(delegate).onQuit()
  }

  @Test
  fun `test that goBack cancels payment`() {
    fragment.loadableView.onGoBack?.invoke()
    verify(spyPaymentSdk).cancelPayment()
  }

  @Test
  fun `test that onRetry reloads Webview`() {
    // Simulate error
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView, mock { on { url } doReturn Uri.parse(initialPageData.url) }, mock())
    assertLoadableViewStatus(Failed())
    finishPageLoad()
    assertLoadableViewStatus(Failed())

    // Reload
    assertEquals(0, shadowWebView.reloadInvocations)
    fragment.loadableView.onRetry?.invoke()
    assertEquals(1, shadowWebView.reloadInvocations)
    assertEquals(Loading(referrer = initialPageData.url), fragment.loadableView.status)
  }

  @Test
  fun `test statusBar color is updated from gradient-theme-color when both meta headers are correct`() {
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
      shadowWebView.jsCallbacks[javascriptThemeScript] =
          "{\"gradientThemeColor\":\"(start-color: #721053, end-color: #AD2E41)\",\"solidThemeColor\":\"#8d204a\"}"
      finishPageLoad()
      verify(delegate)
          .updateStatusBarColor(
              GradientThemeColor(Color.parseColor("#721053"), Color.parseColor("#AD2E41")))
    }
  }

  @Test
  fun `test statusBar color is updated from solid-theme-color when gradient-theme-color is null or malformed`() {
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
      shadowWebView.jsCallbacks[javascriptThemeScript] = "{\"solidThemeColor\":\"#8d204a\"}"
      finishPageLoad()
      verify(delegate).updateStatusBarColor(SolidThemeColor(Color.parseColor("#8d204a")))
    }
  }

  @Test
  fun `test statusBar color is updated from ixigo-theme-color when both meta headers are incorrect`() {
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
      shadowWebView.jsCallbacks[javascriptThemeScript] = "{}"
      finishPageLoad()
      verify(delegate, never()).updateStatusBarColor(SolidThemeColor(Color.parseColor("#FF0000")))
    }
  }

  @Test
  fun `test statusBar color is updated from gradient-theme-color`() {
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
      shadowWebView.jsCallbacks[javascriptThemeScript] =
          "{\"gradientThemeColor\":\"(start-color: #721053, end-color: #AD2E41)\",\"solidThemeColor\":\"#8d204a\"}"
      finishPageLoad()
      verify(delegate)
          .updateStatusBarColor(
              GradientThemeColor(Color.parseColor("#721053"), Color.parseColor("#AD2E41")))
    }
  }

  @Test
  fun `test statusBar color is updated from solid-theme-color`() {
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
      shadowWebView.jsCallbacks[javascriptThemeScript] = "{\"solidThemeColor\":\"#8d204a\"}"
      finishPageLoad()
      verify(delegate).updateStatusBarColor(SolidThemeColor(Color.parseColor("#8d204a")))
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

  @Test
  fun `test app does not crash if there is no app to handle a custom url`() {
    val uri = Uri.parse("unknown://test")
    shadowOf(getApplicationContext<Application>()).checkActivities(true)
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView, mock<WebResourceRequest> { on { url } doReturn uri })
    assertNull(shadowActivity.nextStartedActivity)
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
    finishPageLoad()
    assertEquals(
        """
        if (!window.IxigoSDK) {
          var loadIxigoSDK = function() {
              var script = document.createElement("script");
              script.type = "text/javascript";
              script.src = "${BuildConfig.JS_SDK_URL}";
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

  @Test
  fun `test uiConfig is updated`() {
    shadowWebView.webViewClient.doUpdateVisitedHistory(fragment.webView, initialPageData.url, false)
    val defaultUIConfig = UIConfig(backNavigationMode = BackNavigationMode.Enabled())
    assertEquals("uiConfig has default value", defaultUIConfig, fragment.uiConfig)

    val updatedUIConfig = UIConfig(backNavigationMode = BackNavigationMode.Disabled())
    fragment.configUI(updatedUIConfig)
    assertEquals("uiConfig has been updated", updatedUIConfig, fragment.uiConfig)

    val nextUrl = "https://www.ixigo.com/nextUrl"
    fragment.webView.loadUrl(nextUrl)
    shadowWebView.webViewClient.doUpdateVisitedHistory(fragment.webView, nextUrl, false)
    assertEquals("uiConfig is back to default value", defaultUIConfig, fragment.uiConfig)

    shadowWebView.webViewClient.doUpdateVisitedHistory(fragment.webView, initialPageData.url, false)
    assertEquals("uiConfig resets previously set value", updatedUIConfig, fragment.uiConfig)
  }

  @Test
  fun `test if publishEvent is called successfully after shouldOverrideUrlLoading`() {
    shadowWebView.webViewClient.shouldOverrideUrlLoading(
        fragment.webView,
        mock<WebResourceRequest> { on { url } doReturn Uri.parse(initialPageData.url) })
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
    verify(mockAnalyticsProvider)
        .logEvent(
            event =
                argThat { event ->
                  event.name == "WEBVIEW_INIT_START" &&
                      event.properties["request"] != null &&
                      event.properties["Authorization"] != null
                })
  }

  @Test
  fun `test if publishEvent is called successfully after onPageFinished`() {
    finishPageLoad()
    verify(mockAnalyticsProvider)
        .logEvent(
            event =
                argThat { event ->
                  event.name == "WEBVIEW_INIT_END" &&
                      event.properties["request"] != null &&
                      event.properties["Authorization"] != null
                })
  }

  @Test
  fun `test that webview does not move to loaded state on page load finish if its ixigo url`() {
    shadowWebView.webViewClient.onPageFinished(fragment.webView, "https://www.ixigo.com")
    assertLoadableViewStatus(Loading(referrer = initialPageData.url))
  }

  @Test
  fun `test that webview moves to loaded state on page load finish if its not an ixigo url`() {
    shadowWebView.webViewClient.onPageFinished(fragment.webView, "https://www.random.com")
    assertLoadableViewStatus(Loaded)
  }

  @Test
  fun `test webview injects bundled js sdk script when remote fails`() {
    val scriptRemoteUrl = BuildConfig.JS_SDK_URL
    shadowWebView.webViewClient.onReceivedError(
        fragment.webView, mock { on { url } doReturn Uri.parse(scriptRemoteUrl) }, mock())

    val assetFileReader = AssetFileReader(fragment.requireContext())
    val expectedScript = assetFileReader.readFile("ixigo-sdk.js")

    assertEquals(expectedScript, shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `test webview is destroyed on view detach`() {
    scenario.moveToState(Lifecycle.State.DESTROYED)
    assertTrue(shadowWebView.wasDestroyCalled())
  }

  private fun finishPageLoad() {
    shadowWebView.webViewClient.onPageFinished(fragment.webView, initialPageData.url)
    if (isIxigoUrl(initialPageData.url)) {
      fragment.pwaReady()
    }
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
