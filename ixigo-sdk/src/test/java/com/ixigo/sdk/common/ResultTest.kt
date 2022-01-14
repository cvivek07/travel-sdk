package com.ixigo.sdk.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ResultTest {
  @Test
  fun testOkEquals() {
    assertEquals(Ok("hello"), Ok("hello"))
    assertNotEquals(Ok("hello"), Ok("goodbye"))
    assertNotEquals(Ok("hello"), "hello")
  }

  @Test
  fun testOkHashcode() {
    assertEquals("hello".hashCode(), Ok("hello").hashCode())
  }

  @Test
  fun testErrEquals() {
    assertEquals(Err("error1"), Err("error1"))
    assertNotEquals(Err("error1"), Error("error2"))
  }

  @Test
  fun testErrHashcode() {
    val error = Error("error1")
    assertEquals(error.hashCode(), Err(error).hashCode())
  }
}
