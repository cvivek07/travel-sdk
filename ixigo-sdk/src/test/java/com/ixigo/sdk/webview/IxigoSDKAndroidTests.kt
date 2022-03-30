package com.ixigo.sdk.webview

import IxigoSDKAndroid
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.sms.OtpSmsRetriever
import com.ixigo.sdk.sms.OtpSmsRetrieverCallback
import com.ixigo.sdk.sms.OtpSmsRetrieverError
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
      ixigoSDKAndroid = IxigoSDKAndroid(mockAnalyticsProvider, it, mockOtpSmsRetriever)
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
      |}"""".trimMargin(),
        "success:TO_REPLACE_PAYLOAD",
        "error:TO_REPLACE_PAYLOAD")
    assertEquals(
        """error:{\"errorCode\":\"InvalidArgumentError\",\"errorMessage\":\"unable to parse input={\\n\\\"backNavigationEnabled\\\": {\\n  \\\"type\\\": \\\"unknownType\\\" \\n}\\n}\\\"\"}""",
        shadowWebView.lastEvaluatedJavascript)
  }
}
