package com.ixigo.sdk.webview

import android.os.Bundle
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.test.initializeTestIxigoSDK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class HtmlOutJsInterfaceTests {

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))
  private lateinit var shadowWebView: ShadowWebView
  private lateinit var fragment: WebViewFragment

  private lateinit var htmlOut: HtmlOutJsInterface
  private lateinit var mockPartnerTokenProvider: PartnerTokenProvider

  @Before
  fun setup() {
    initializeTestIxigoSDK()
    mockPartnerTokenProvider = mock()
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
            })
    scenario.onFragment {
      fragment = it
      shadowWebView = shadowOf(it.webView)
      shadowWebView.pushEntryToHistory(initialPageData.url)
      htmlOut = HtmlOutJsInterface(it, mockPartnerTokenProvider)
    }
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test successful token fetch`() {
    testLogin("myToken")
  }

  @Test
  fun `test failed token fetch`() {
    testLogin(null)
  }

  @Test
  fun `test failed incorrect input json`() {
    htmlOut.invokeSSOLogin("Wrong Json")
    assertEquals(initialPageData.url, shadowWebView.lastLoadedUrl)
    verifyNoInteractions(mockPartnerTokenProvider)
  }

  private fun testLogin(token: String?) {
    doAnswer {
          val callback: PartnerTokenCallback = it.getArgument(1)
          if (token == null) {
            callback(Err(PartnerTokenErrorSDK()))
          } else {
            callback(Ok(PartnerToken(token)))
          }
          true
        }
        .`when`(mockPartnerTokenProvider)
        .fetchPartnerToken(eq(PartnerTokenProvider.Requester.CUSTOMER), any())

    val jsonInput =
        """{
      |"callback": "myCallback",
      |"provider": "myProvider",
      |"promiseId": "myPromiseId"
      |}""".trimMargin()

    val invokeSSOLoginMethod =
        htmlOut.javaClass.getDeclaredMethod("invokeSSOLogin", String::class.java)
    assertNotNull(invokeSSOLoginMethod.getAnnotation(JavascriptInterface::class.java))

    invokeSSOLoginMethod.invoke(htmlOut, jsonInput)
    shadowOf(Looper.getMainLooper()).idle()
    val expectedUrl =
        if (token == null)
            """javascript:{myCallback('{"promiseId":"myPromiseId","data":{"responseCode":104,"errorMessage":"SDK Error"}}');};"""
        else
            """javascript:{myCallback('{"promiseId":"myPromiseId","data":{"responseCode":200,"grantToken":"$token"}}');};"""
    assertEquals(expectedUrl, shadowWebView.lastLoadedUrl)
  }
}
