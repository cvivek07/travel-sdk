package com.ixigo.sdk.webview

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
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
    scenario =
        launchFragmentInContainer(
            Bundle().also {
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
  fun `test that IxiWebView is loaded`() {
    val ixiWebView: IxiWebView = shadowWebView.getJavascriptInterface("IxiWebView") as IxiWebView
    assertNotNull(ixiWebView)
  }
}
