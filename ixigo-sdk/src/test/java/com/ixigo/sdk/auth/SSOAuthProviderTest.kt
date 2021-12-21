package com.ixigo.sdk.auth

import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.test.FakeAnalyticsProvider
import com.ixigo.sdk.auth.test.FakePartnerTokenProvider
import com.ixigo.sdk.payment.EmptyPaymentProvider
import com.ixigo.sdk.test.TestData.FakeAppInfo
import java.util.concurrent.CountDownLatch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.awaitility.kotlin.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SSOAuthProviderTest {

  private lateinit var mockServer: MockWebServer

  @Before
  fun setup() {
    mockServer = MockWebServer()
    mockServer.start()
    IxigoSDK.replaceInstance(
        IxigoSDK(
            FakeAppInfo,
            EmptyAuthProvider,
            EmptyPaymentProvider,
            FakeAnalyticsProvider(),
            Config(mockServer.url("").toString())))
  }

  @After
  fun tearDown() {
    IxigoSDK.clearInstance()
  }

  @Test
  fun `test that accessToken is retrieved successfully`() {
    val expectedAccessToken = "expectedAccessToken"
    mockServer.enqueue(MockResponse().setBody(validJsonResponse(expectedAccessToken)))
    val countDownLatch = CountDownLatch(1)
    val partnerToken = PartnerToken("partnerToken")
    val ssoAuthProvider = SSOAuthProvider(FakePartnerTokenProvider(partnerToken))
    assertNull(ssoAuthProvider.authData)

    var callbackCalled = false
    launchActivity<FragmentActivity>().onActivity { activity ->
      val handled =
          ssoAuthProvider.login(activity) {
            assertTrue(it.isSuccess)
            it.onSuccess { authData -> assertEquals(expectedAccessToken, authData.token) }
            assertRequest(partnerToken)
            callbackCalled = true
          }
      assertTrue(handled)
    }
    await.until { callbackCalled }
  }

  @Test
  fun `test that login returns false if no partnerToken is provided`() {
    val ssoAuthProvider = SSOAuthProvider(FakePartnerTokenProvider(null))

    launchActivity<FragmentActivity>().onActivity { activity ->
      val handled = ssoAuthProvider.login(activity) { fail("block should not have been called") }
      assertFalse(handled)
      assertEquals(0, mockServer.requestCount)
    }
  }

  @Test
  fun `test that login returns Error if request returns 400`() {
    assertRequestFails(MockResponse().setResponseCode(400))
  }

  @Test
  fun `test that login returns Error if json is invalid`() {
    assertRequestFails(MockResponse().setBody("""{"data":{}}"""))
  }

  @Test
  fun `test that login returns Error if request fails`() {
    assertRequestFails(MockResponse().setStatus("bad status"))
  }

  private fun assertRequestFails(response: MockResponse) {
    mockServer.enqueue(response)
    val partnerToken = PartnerToken("partnerToken")
    val ssoAuthProvider = SSOAuthProvider(FakePartnerTokenProvider(partnerToken))

    var callbackCalled = false
    launchActivity<FragmentActivity>().onActivity { activity ->
      val handled =
          ssoAuthProvider.login(activity) {
            print("XXX inside UI Thread")
            assertFalse(it.isSuccess)
            assertRequest(partnerToken)
            callbackCalled = true
            print("callback changed")
          }
      assertTrue(handled)
    }
    await.until { callbackCalled }
    print("XXX DONE")
  }

  private fun assertRequest(partnerToken: PartnerToken) {
    assertEquals(1, mockServer.requestCount)
    val request = mockServer.takeRequest()

    assertEquals("POST", request.method)
    assertEquals("/api/v2/oauth/sso/login/token", request.path)
    assertEquals("[text=authCode=${partnerToken.token}]", request.body.toString())

    val appInfo = IxigoSDK.getInstance().appInfo

    assertTrue(request.headers.contains(Pair("ixiSrc", appInfo.clientId)))
    assertTrue(request.headers.contains(Pair("clientId", appInfo.clientId)))
    assertTrue(request.headers.contains(Pair("apiKey", appInfo.apiKey)))
    assertTrue(request.headers.contains(Pair("deviceId", appInfo.deviceId)))
  }

  fun validJsonResponse(accessToken: String): String =
      """
        {
          "data": {
            "token_type": "bearer",
            "refresh_token": "NA",
            "uid": "1234567890",
            "info": {
              "name": "Bugs Bunny",
              "fn": "Bugs",
              "ln": "Bunny",
              "userType": "INTERNAL",
              "isIxigoUser": true,
              "email": "bugs.bunny@looneytoones.com",
              "emailVerified": true,
              "mobile": "123456789",
              "prefix": "+1",
              "isPhNoVerified": true,
              "emailUpdateRequired": false,
              "isEmailUsable": true,
              "en": true,
              "handle": null
            },
            "creationDate": 1621450139174,
            "access_token": "$accessToken",
            "expires_in": 15552000,
            "scope": "ANDROID_TRAIN"
          }
        }"""
}
