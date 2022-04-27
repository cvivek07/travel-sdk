package com.ixigo.sdk.payment

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.remoteConfig.FakeRemoteConfigProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class DefaultPaymentProviderTests {

  @get:Rule val activityRule = ActivityScenarioRule(FragmentActivity::class.java)

  @get:Rule val mockitoRule: MockitoRule = MockitoJUnit.rule()

  lateinit var activity: FragmentActivity

  lateinit var fakeRemoteConfigProvider: FakeRemoteConfigProvider

  @Mock lateinit var mockHostAppPaymentPaymentProvider: PaymentProviderAndHandler
  @Mock lateinit var mockWebPaymentProvider: PaymentProviderAndHandler
  @Mock lateinit var mockSDKPaymentProvider: PaymentProviderAndHandler

  private lateinit var paymentProvider: DefaultPaymentProvider

  @Before
  fun setup() {
    fakeRemoteConfigProvider = FakeRemoteConfigProvider()
    paymentProvider =
        DefaultPaymentProvider(fakeRemoteConfigProvider, mockHostAppPaymentPaymentProvider) {
          when (it) {
            PaymentMode.WEB -> mockWebPaymentProvider
            PaymentMode.SDK -> mockSDKPaymentProvider
          }
        }
    activityRule.scenario.onActivity { activity = it }
  }

  @Test
  fun `test Host App Payment provider is used if specified in remote config`() {
    assertProvider(
        mockHostAppPaymentPaymentProvider,
        PaymentRemoteConfig(mode = PaymentMode.WEB, allowHostAppPayment = true))
  }

  @Test
  fun `test Host App Payment provider is used if remoteConfig is empty`() {
    assertProvider(mockHostAppPaymentPaymentProvider, null)
  }

  @Test
  fun `test PaymentProvider is disabled if paymentMode is WEB`() {
    assertProvider(
        mockWebPaymentProvider,
        PaymentRemoteConfig(mode = PaymentMode.WEB, allowHostAppPayment = false))
  }

  @Test
  fun `test PaymentSDK is used if paymentMode is SDK`() {
    assertProvider(
        mockSDKPaymentProvider,
        PaymentRemoteConfig(mode = PaymentMode.SDK, allowHostAppPayment = false))
  }

  private fun assertProvider(delegatedProvider: PaymentProvider, config: PaymentRemoteConfig?) {
    config?.let { fakeRemoteConfigProvider.values["payment"] = it }
    val paymentInput = PaymentInput("product", data = mapOf("prop1" to "value1"))
    val callback: PaymentCallback = {}

    paymentProvider.startPayment(activity, paymentInput, callback)
    verify(delegatedProvider).startPayment(eq(activity), same(paymentInput), eq(callback))

    val requestCode = 12
    val resultCode = 13
    val data = Intent()
    paymentProvider.handle(requestCode = requestCode, resultCode = resultCode, data = data)
    verify(delegatedProvider as ActivityResultHandler)
        .handle(eq(requestCode), eq(resultCode), same(data))
  }
}

interface PaymentProviderAndHandler : PaymentProvider, ActivityResultHandler
