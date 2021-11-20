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
    fun testErrEquals() {
        assertEquals(Err(Error("error1")), Err(Error("error1")))
        assertEquals(Err(Error("error1")), Err(Error("error2")))
        assertNotEquals(Err(Error("error1")), Error("error1"))
    }
}