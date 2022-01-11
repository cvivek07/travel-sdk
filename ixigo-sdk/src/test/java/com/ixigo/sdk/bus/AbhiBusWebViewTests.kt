package com.ixigo.sdk.bus

import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AbhiBusWebViewTests {

  private val initialPageData =
      InitialPageData("https://www.abhibus.com", mapOf("header1" to "header1Value"))
  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private lateinit var abhiBusWebView: AbhiBusWebView

  @Before
  fun setup() {
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment { abhiBusWebView = AbhiBusWebView(it) }
  }

  @Test
  fun `test say Hello returns correct string`() {
    val sayHello = abhiBusWebView.javaClass.getDeclaredMethod("sayHello")
    Assert.assertNotNull(sayHello.getAnnotation(JavascriptInterface::class.java))
    assertEquals("Hello from Abhibus", abhiBusWebView.sayHello())
  }
}
