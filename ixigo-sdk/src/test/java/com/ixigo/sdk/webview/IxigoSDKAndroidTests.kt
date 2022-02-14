package com.ixigo.sdk.webview

import IxigoSDKAndroid
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.initializeTestIxigoSDK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

@RunWith(AndroidJUnit4::class)
class IxigoSDKAndroidTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: FragmentScenario<WebViewFragment>
  private lateinit var fragment: WebViewFragment
  private lateinit var ixigoSDKAndroid: IxigoSDKAndroid

  @Mock private lateinit var mockAnalyticsProvider: AnalyticsProvider

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
      ixigoSDKAndroid = IxigoSDKAndroid(mockAnalyticsProvider, it)
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
}
