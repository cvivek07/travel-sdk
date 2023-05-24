package com.ixigo.sdk.payment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.auth.test.FakePartnerTokenProvider
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.test.assertLaunchedIntent
import com.ixigo.sdk.test.defaultIntentHeaders
import com.ixigo.sdk.test.initializePaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class PaymentSDKTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: ActivityScenario<FragmentActivity>
  private lateinit var activity: Activity

  @Mock lateinit var mockAnalyticsProvider: AnalyticsProvider
  @Mock lateinit var urlLoader: UrlLoader
  @Mock lateinit var ssoAuthProvider: SSOAuthProvider

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
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=1&txnId=transactionIdValue&flowType=PAYMENT_SDK")
  }

  @Test
  fun `test processPayment with gateway`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        gatewayId = "gatewayIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=gatewayIdValue&txnId=transactionIdValue&flowType=PAYMENT_SDK")
  }

  @Test
  fun `test processPayment with urlLoader`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=1&txnId=transactionIdValue&flowType=PAYMENT_SDK",
        expectedHeaders =
            mapOf(
                "appVersion" to "1",
                "clientId" to "clientId",
                "apiKey" to "apiKey",
                "deviceId" to "deviceId",
                "uuid" to "uuid",
                "Authorization" to "token"),
        partnerToken = "token",
        urlLoader = urlLoader)
  }

  @Test
  fun `test processPayment with flowType`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=1&txnId=transactionIdValue&flowType=OTHER",
        flowType = "OTHER")
  }

  @Test
  fun `test processPayment with tripId and providerId`() {
    assertProcessPayment(
        transactionId = "transactionIdValue",
        providerId = "providerIdValue",
        tripId = "tripIdValue",
        expectedUrl =
            "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=PAYMENT&gatewayId=1&txnId=transactionIdValue&flowType=PAYMENT_SDK&tripId=tripIdValue&providerId=providerIdValue")
  }

  @Test
  fun `test processPayment returns error if unable to get ixigo token`() {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)

    scenario.onActivity { activity ->
      whenever(ssoAuthProvider.login(same(activity), eq(IxigoSDK.instance.appInfo.clientId), any()))
          .then { invocation ->
            val callback: AuthCallback = invocation.getArgument(2)
            callback.invoke(Err(Error("errorMessage")))
            true
          }
      var result: ProcessPaymentResult? = null
      PaymentSDK.instance.processPayment(activity, transactionId = "transactionIdValue") {
        result = it
      }
      assertEquals("errorMessage", (result as Err<ProcessPaymentNotLoginError>).value.error.message)
      verifyNoInteractions(mockAnalyticsProvider)
    }
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

  @Test
  fun `test openManagePaymentPage returns error if unable to get ixigo token`() {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)

    scenario.onActivity { activity ->
      whenever(ssoAuthProvider.login(same(activity), eq(IxigoSDK.instance.appInfo.clientId), any()))
          .then { invocation ->
            val callback: AuthCallback = invocation.getArgument(2)
            callback.invoke(Err(Error("errorMessage")))
            true
          }
      var result: OpenPageResult? = null
      PaymentSDK.instance.openManagePaymentMethodsPage(activity) { result = it }
      assertEquals(
          "errorMessage", (result as Err<OpenPageUserNotLoggedInError>).value.error.message)
      verifyNoInteractions(mockAnalyticsProvider)
    }
  }

  @Test
  fun `test openManagePaymentMethods Page is opened`() {
    initializeTestIxigoSDK(
        analyticsProvider = mockAnalyticsProvider,
        partnerTokenProvider = FakePartnerTokenProvider("iximatr", PartnerToken("token")))
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)
    scenario.onActivity { activity ->
      whenever(ssoAuthProvider.login(same(activity), eq(IxigoSDK.instance.appInfo.clientId), any()))
          .then {
            val callback: AuthCallback = it.getArgument(2)
            callback.invoke(Ok(AuthData(token = "token")))
            true
          }

      PaymentSDK.instance.openManagePaymentMethodsPage(activity)
      val authHeaders = mapOf("Authorization" to "token")

      assertLaunchedIntent(
          activity,
          url =
              "https://www.ixigo.com/pwa/initialpage?clientId=clientId&apiKey=apiKey&appVersion=1&deviceId=deviceId&languageCode=en&page=MANAGE_PAYMENT_METHODS",
          expectedHeaders = defaultIntentHeaders + authHeaders)
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
      expectedHeaders: Map<String, String>? = null,
      funnelConfig: FunnelConfig? = null,
      partnerToken: String? = null,
      urlLoader: UrlLoader? = null,
      flowType: String? = null,
      tripId: String? = null,
      providerId: String? = null,
      productType: String? = null
  ) {
    initializeTestIxigoSDK(
        analyticsProvider = mockAnalyticsProvider,
        partnerTokenProvider =
            FakePartnerTokenProvider("iximatr", partnerToken?.let { PartnerToken(it) }))
    initializePaymentSDK(ssoAuthProvider = ssoAuthProvider)
    scenario.onActivity { activity ->
      whenever(ssoAuthProvider.login(same(activity), eq(IxigoSDK.instance.appInfo.clientId), any()))
          .then {
            val callback: AuthCallback = it.getArgument(2)
            callback.invoke(Ok(AuthData(token = "token")))
            true
          }
      if (gatewayId != null) {
        if (flowType != null) {
          PaymentSDK.instance.processPayment(
              activity,
              transactionId = transactionId,
              gatewayId = gatewayId,
              config = funnelConfig,
              flowType = flowType,
              urlLoader = urlLoader)
        } else {
          PaymentSDK.instance.processPayment(
              activity,
              transactionId = transactionId,
              gatewayId = gatewayId,
              config = funnelConfig,
              urlLoader = urlLoader)
        }
      } else {
        if (flowType != null) {
          PaymentSDK.instance.processPayment(
              activity,
              transactionId = transactionId,
              config = funnelConfig,
              urlLoader = urlLoader,
              flowType = flowType)
        } else {
          PaymentSDK.instance.processPayment(
              activity,
              transactionId = transactionId,
              config = funnelConfig,
              urlLoader = urlLoader,
              tripId = tripId,
              providerId = providerId,
              productType = productType)
        }
      }
      val authHeaders = mapOf("Authorization" to "token")
      if (urlLoader != null) {
        verify(urlLoader).loadUrl(expectedUrl, expectedHeaders)
      } else {
        assertLaunchedIntent(
            activity, expectedUrl, expectedHeaders = defaultIntentHeaders + authHeaders)
      }

      verify(mockAnalyticsProvider).logEvent(Event.with(action = "paymentsStartHome"))
    }
  }
}
