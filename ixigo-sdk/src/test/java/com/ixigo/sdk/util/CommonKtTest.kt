package com.ixigo.sdk.util

import org.junit.Assert.*
import org.junit.Test

class CommonKtTest {

  @Test
  fun `test isIxigoUrl`() {
    assertTrue(isIxigoUrl("https://www.ixigo.com"))
    assertTrue(isIxigoUrl("https://build6.ixigo.com"))
    assertFalse(isIxigoUrl("https://random.com"))
    assertFalse(isIxigoUrl("abc"))
  }
}