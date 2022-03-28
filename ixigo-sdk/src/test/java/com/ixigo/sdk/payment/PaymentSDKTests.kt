package com.ixigo.sdk.payment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.initializePaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.FunnelConfig
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.JsInterface
import com.ixigo.sdk.webview.WebViewFragment
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class PaymentSDKTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: ActivityScenario<Activity>
  private lateinit var activity: Activity

  @Mock lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    IxigoSDK.clearInstance()
    PaymentSDK.clearInstance()
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
    PaymentSDK.clearInstance()
  }

  @Test
  fun `test processPayment without gateway`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=1&txnId=transactionIdValue")
  }

  @Test
  fun `test processPayment with gateway`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        gatewayId = "gatewayIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=gatewayIdValue&txnId=transactionIdValue")
  }

  @Test
  fun `test that PaymentJsInterface is added to Js Interfaces for ixigo url`() {
    testJsInterface(Config.ProdConfig.createUrl("testUrl")) { interfaces ->
      Assert.assertTrue(interfaces.any { (it as? PaymentJsInterface) != null })
    }
  }

  @Test
  fun `test that PaymentJsInterface is added to Js Interfaces for file url`() {
    testJsInterface("file://anything") { interfaces ->
      Assert.assertTrue(interfaces.any { (it as? PaymentJsInterface) != null })
    }
  }

  @Test
  fun `test that PaymentJsInterface is NOT added to Js Interfaces for Non ixigo url`() {
    testJsInterface("https://www.confirmtkt.com/test") { interfaces ->
      Assert.assertFalse(interfaces.any { (it as? PaymentJsInterface) != null })
    }
  }

  private fun testJsInterface(url: String, check: (List<JsInterface>) -> Unit) {
    initializeTestIxigoSDK()
    PaymentSDK.init()
    val scenario: FragmentScenario<WebViewFragment> =
        launchFragmentInContainer(
            Bundle().also {
              it.putParcelable(
                  WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData("https://www.ixigo.com"))
            })
    scenario.onFragment { webViewFragment ->
      val interfaces = IxigoSDK.instance.webViewConfig.getMatchingJsInterfaces(url, webViewFragment)
      check(interfaces)
    }
  }

  private fun assertProcessPayment(
      transactionId: String,
      gatewayId: String? = null,
      expectedUrl: String,
      funnelConfig: FunnelConfig? = null
  ) {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    initializePaymentSDK()
    scenario.onActivity { activity ->
      if (gatewayId != null) {
        PaymentSDK.instance.processPayment(
            activity, transactionId = transactionId, gatewayId = gatewayId, config = funnelConfig)
      } else {
        PaymentSDK.instance.processPayment(
            activity, transactionId = transactionId, config = funnelConfig)
      }
      assertLaunchedIntent(activity, expectedUrl)
      verify(mockAnalyticsProvider).logEvent(Event.with(action = "paymentsStartHome"))
    }
  }
}
