package com.ixigo.sdk.payment

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.juspay.services.HyperServices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class DefaultPaymentGatewayProviderTests {

  @get:Rule val activityRule = ActivityScenarioRule(FragmentActivity::class.java)

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  lateinit var fragmentActivity: FragmentActivity
  lateinit var paymentGatewayProvider: DefaultPaymentGatewayProvider
  lateinit var config: PaymentConfig

  @Mock lateinit var hyperInstanceFactory: HyperInstanceFactory
  @Mock lateinit var hyperInstance: HyperServices

  @Before
  fun setup() {
    Mockito.`when`(hyperInstanceFactory.create(any())).thenReturn(hyperInstance)

    activityRule.scenario.onActivity {
      fragmentActivity = it
      config = PaymentConfig(juspayConfig = JuspayConfig(environment = JusPayEnvironment.SANDBOX))
      paymentGatewayProvider = DefaultPaymentGatewayProvider(config, hyperInstanceFactory)
    }
  }

  @Test
  fun `jusPayGateway is returned for JUSPAY`() {
    val jusPayGateway =
        paymentGatewayProvider.getPaymentGateway("JUSPAY", fragmentActivity) as JusPayGateway
    assertEquals(config.juspayConfig.environment, jusPayGateway.environment)
  }

  @Test
  fun `null is returned for unknown provider`() {
    val jusPayGateway = paymentGatewayProvider.getPaymentGateway("Unknown", fragmentActivity)
    assertNull(jusPayGateway)
  }
}
