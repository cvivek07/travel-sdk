package com.ixigo.sdk.webview

import IxigoSDKAndroid
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.EmptyPartnerTokenProvider
import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.auth.test.FakePartnerTokenProvider
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.common.CustomChromeTabsHelper
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.sms.OtpSmsRetriever
import com.ixigo.sdk.sms.OtpSmsRetrieverCallback
import com.ixigo.sdk.sms.OtpSmsRetrieverError
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.test.initializeTestIxigoSDK
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
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class IxigoSDKAndroidTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private lateinit var fragment: WebViewFragment
  private lateinit var ixigoSDKAndroid: IxigoSDKAndroid
  private lateinit var shadowWebView: ShadowWebView

  @Mock private lateinit var mockAnalyticsProvider: AnalyticsProvider
  @Mock private lateinit var mockOtpSmsRetriever: OtpSmsRetriever
  @Mock private lateinit var busSDK: BusSDK
  @Mock private lateinit var mockCustomChromeTabsHelper: CustomChromeTabsHelper
  private lateinit var fakePartnerTokenProvider: FakePartnerTokenProvider

  @Before
  fun setup() {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    scenario =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(
                  WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData("https://www.ixigo.com"))
            })
    scenario.onFragment {
      fragment = it
      shadowWebView = shadowOf(fragment.webView)
      fakePartnerTokenProvider = FakePartnerTokenProvider()
      ixigoSDKAndroid =
          IxigoSDKAndroid(
              mockAnalyticsProvider,
              it,
              mockOtpSmsRetriever,
              fakePartnerTokenProvider,
              mockCustomChromeTabsHelper)
    }
  }

  @Test
  fun `logEvent logs event correctly for event with no properties`() {
    ixigoSDKAndroid.logEvent("""{"name": "nameValue"}""")
    verify(mockAnalyticsProvider)
        .logEvent(Event(name = "nameValue", referrer = "https://www.ixigo.com"))
  }

  @Test
  fun `logEvent logs event correctly for event with properties`() {
    ixigoSDKAndroid.logEvent(
        """{
      |"name": "nameValue", 
      |"properties": {
      |   "prop1": "prop1Value",
      |   "prop2": "prop2Value"
      | }
      |}""".trimMargin())
    verify(mockAnalyticsProvider)
        .logEvent(
            Event(
                name = "nameValue",
                properties = mapOf("prop1" to "prop1Value", "prop2" to "prop2Value"),
                referrer = "https://www.ixigo.com"))
  }

  @Test
  fun `logEvent logs NO event for malformed event`() {
    ixigoSDKAndroid.logEvent("""{"nameWrong": "nameValue"}""")
    verifyNoMoreInteractions(mockAnalyticsProvider)
  }

  @Test
  fun `logEvent logs NO event for malformed JSON`() {
    ixigoSDKAndroid.logEvent("""{""")
    verifyNoMoreInteractions(mockAnalyticsProvider)
  }

  @Test
  fun `readSms returns sms content`() {
    whenever(mockOtpSmsRetriever.startListening(any())).then {
      val callback = it.getArgument<OtpSmsRetrieverCallback>(0)
      callback(Ok("smsContentValue"))
    }
    ixigoSDKAndroid.readSms("success:TO_REPLACE_PAYLOAD", "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """success:{\"smsContent\":\"smsContentValue\"}""", shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `readSms returns sms error`() {
    whenever(mockOtpSmsRetriever.startListening(any())).then {
      val callback = it.getArgument<OtpSmsRetrieverCallback>(0)
      callback(Err(OtpSmsRetrieverError.CONSENT_DENIED))
    }
    ixigoSDKAndroid.readSms("success:TO_REPLACE_PAYLOAD", "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """error:{\"errorCode\":\"ConsentDenied\"}""", shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `forwards activity result handle to OtpSmsRetriever`() {
    val requestCode = 1
    val resultCode = 2
    val intent = Intent()
    ixigoSDKAndroid.handle(requestCode, resultCode, intent)
    verify(mockOtpSmsRetriever).handle(requestCode, resultCode, intent)
  }

  @Test
  fun `launchAdditionalBusTrips works if BusSDK is initialized`() {
    BusSDK.replaceInstance(busSDK)
    val ret = ixigoSDKAndroid.openAdditionalBusTrips()
    verify(busSDK).launchAdditionalTrips(fragment.requireContext())
    assertTrue(ret)
  }

  @Test
  fun `launchAdditionalBusTrips returns false if BusSDK is not initialized`() {
    BusSDK.clearInstance()
    val ret = ixigoSDKAndroid.openAdditionalBusTrips()
    verify(busSDK, never()).launchAdditionalTrips(fragment.requireContext())
    assertFalse(ret)
  }

  @Test
  fun `configureUI sets configUI in webFragment`() {
    ixigoSDKAndroid.configureUI(
        """{
      |"backNavigationMode": {
      |  "type": "handler"
      |}
      |}""".trimMargin(),
        "success:TO_REPLACE_PAYLOAD",
        "error:TO_REPLACE_PAYLOAD")
    assertEquals("""success:{}""", shadowWebView.lastEvaluatedJavascript)
    assertEquals(UIConfig(backNavigationMode = BackNavigationMode.Handler()), fragment.uiConfig)
  }

  @Test
  fun `configureUI returns error for wrong input`() {
    ixigoSDKAndroid.configureUI(
        """{
      |"backNavigationEnabled": {
      |  "type": "unknownType" 
      |}
      |}""".trimMargin(),
        "success:TO_REPLACE_PAYLOAD",
        "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={\\n\\\"backNavigationEnabled\\\": {\\n  \\\"type\\\": \\\"unknownType\\\" \\n}\\n}\"}""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `fetchPartnerToken returns authToken correctly`() {
    fakePartnerTokenProvider.partnerTokenMap[
        PartnerTokenProvider.Requester(
            "partnerIdValue", PartnerTokenProvider.RequesterType.CUSTOMER)] =
        PartnerToken("authTokenValue")
    ixigoSDKAndroid.fetchPartnerToken(
        """{
      |"partnerId": "partnerIdValue"
      |}""".trimMargin(),
        "success:TO_REPLACE_PAYLOAD",
        "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """success:{\"authToken\":\"authTokenValue\"}""", shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `fetchPartnerToken returns partner token error correctly`() {
    ixigoSDKAndroid.fetchPartnerToken(
        """{
      |"partnerId": "partnerIdValue"
      |}""".trimMargin(),
        "success:TO_REPLACE_PAYLOAD",
        "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """error:{\"errorCode\":\"104\",\"errorMessage\":\"SDK Error\"}""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `fetchPartnerToken returns error for wrong input`() {
    ixigoSDKAndroid.fetchPartnerToken(
        """{
      |}""".trimMargin(), "success:TO_REPLACE_PAYLOAD", "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={\\n}\"}""",
        shadowWebView.lastEvaluatedJavascript)
  }

  @Test
  fun `openWindow works when specifying webview browser`() {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn FakeAppInfo
      on { partnerTokenProvider } doReturn EmptyPartnerTokenProvider
    }
    IxigoSDK.replaceInstance(mockIxigoSDK)
    scenario.onFragment { fragment ->
      val url = "https://www.ixigo.com/page1"
      ixigoSDKAndroid.openWindow(url, """{"browser": "webview"}""")
      verify(mockIxigoSDK, times(1)).launchWebActivity(fragment.requireActivity(), url)
    }
  }

  @Test
  fun `openWindow works uses webview when specifying bogus data`() {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn FakeAppInfo
      on { partnerTokenProvider } doReturn EmptyPartnerTokenProvider
    }
    IxigoSDK.replaceInstance(mockIxigoSDK)
    scenario.onFragment { fragment ->
      val url = "https://www.ixigo.com/page1"
      ixigoSDKAndroid.openWindow(url, """{"browser": "bogus"}""")
      verify(mockIxigoSDK, times(1)).launchWebActivity(fragment.requireActivity(), url)
    }
  }

  @Test
  fun `openWindow uses customChromeTabs when specifying native browser`() {
    scenario.onFragment { fragment ->
      val url = "https://www.ixigo.com/page1"
      ixigoSDKAndroid.openWindow(url, """{"browser": "native"}""")
      verify(mockCustomChromeTabsHelper).openUrl(fragment.requireActivity(), url)
    }
  }

  @Test
  fun `openWindow uses webView when options is null`() {
    val mockIxigoSDK: IxigoSDK = mock {
      on { appInfo } doReturn FakeAppInfo
      on { partnerTokenProvider } doReturn EmptyPartnerTokenProvider
    }
    IxigoSDK.replaceInstance(mockIxigoSDK)
    scenario.onFragment { fragment ->
      val url = "https://www.ixigo.com/page1"
      ixigoSDKAndroid.openWindow(url, null)
      verify(mockIxigoSDK, times(1)).launchWebActivity(fragment.requireActivity(), url)
    }
  }
}
