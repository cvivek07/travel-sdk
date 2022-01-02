package com.ixigo.sdk.analytics

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class ChainAnalyticsProviderTest {

  @Mock lateinit var provider1: AnalyticsProvider

  @Mock lateinit var provider2: AnalyticsProvider

  @Test
  fun `test that it forwards events to providers in order`() {
    val chainProvider = ChainAnalyticsProvider(provider1, provider2)
    val event = Event(name = "myEvent")
    chainProvider.logEvent(event)
    val orderVerifier = inOrder(provider1, provider2)

    orderVerifier.verify(provider1).logEvent(event)
    orderVerifier.verify(provider2).logEvent(event)
  }
}
