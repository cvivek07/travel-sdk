package com.ixigo.sdk.payment

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.FunnelConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

  private fun assertProcessPayment(
      transactionId: String,
      gatewayId: String? = null,
      expectedUrl: String,
      funnelConfig: FunnelConfig? = null
  ) {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    scenario.onActivity { activity ->
      if (gatewayId != null) {
        IxigoSDK.instance.processPayment(
            activity, transactionId = transactionId, gatewayId = gatewayId, config = funnelConfig)
      } else {
        IxigoSDK.instance.processPayment(
            activity, transactionId = transactionId, config = funnelConfig)
      }
      assertLaunchedIntent(activity, expectedUrl)
      verify(mockAnalyticsProvider).logEvent(Event.with(action = "paymentsStartHome"))
    }
  }
}
