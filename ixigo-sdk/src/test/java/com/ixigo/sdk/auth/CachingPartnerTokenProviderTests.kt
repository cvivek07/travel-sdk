package com.ixigo.sdk.auth

import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class CachingPartnerTokenProviderTests {

  @Mock lateinit var mockPartnerTokenProvider: PartnerTokenProvider

  private lateinit var cachingPartnerTokenProvider: CachingPartnerTokenProvider

  @Before
  fun setup() {
    cachingPartnerTokenProvider = CachingPartnerTokenProvider(mockPartnerTokenProvider)
  }

  @Test
  fun `test that enabled returns underlying enabled property`() {
    whenever(mockPartnerTokenProvider.enabled).thenReturn(false)
    assertFalse(cachingPartnerTokenProvider.enabled)
  }
}
