package com.ixigo.sdk.bus

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.Config
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.test.initializeTestIxigoSDK
import java.time.LocalDate
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.awaitility.kotlin.await
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheapestFairTests {

  private lateinit var mockServer: MockWebServer

  @Before
  fun setup() {
    mockServer = MockWebServer()
    mockServer.start()
    initializeTestIxigoSDK()
    BusSDK.replaceInstance(BusSDK(config = Config(mockServer.url("").toString())))
  }

  @Test
  fun `test successful call`() {
    mockServer.enqueue(
        MockResponse()
            .setBody(
                """{
"minFare": "400",
"minTravelTime": "6h",
"busSourceName": "Hyderabad",
"busSourceID": "3",
"busDestinationName": "Vijayawada",
"busDestinationID": "5"
}"""))
    val expectedOutput =
        CheapestFareOutput(
            minFare = "400",
            minTravelTime = "6h",
            busSourceName = "Hyderabad",
            busSourceID = "3",
            busDestinationName = "Vijayawada",
            busDestinationID = "5")
    var callbackCalled = false
    BusSDK.instance.getCheapestFair(
        CheapestFareInput("HYD", "VIJ", LocalDate.parse("2022-01-10"))) {
      when (it) {
        is Ok -> assertEquals(expectedOutput, expectedOutput)
        is Err -> Assert.fail("Unexpected Error")
      }
      callbackCalled = true
    }
    await.until { callbackCalled }

    assertEquals(1, mockServer.requestCount)
    val request = mockServer.takeRequest()
    assertEquals("GET", request.method)
    assertEquals("/trainrouteinfo/HYD/VIJ/10-01-2022", request.path)
  }

  @Test
  fun `test error call`() {
    mockServer.enqueue(MockResponse().setStatus("Failed"))
    var callbackCalled = false
    BusSDK.instance.getCheapestFair(
        CheapestFareInput("HYD", "VIJ", LocalDate.parse("2022-01-10"))) {
      when (it) {
        is Ok -> Assert.fail("Expecting Error")
        is Err -> assertEquals(Err(CheapestFairError.HTTP_ERROR), it)
      }
      callbackCalled = true
    }
    await.until { callbackCalled }
  }

  @Test
  fun `test wrong json`() {
    mockServer.enqueue(MockResponse().setBody("""{
"bad": "400",
}"""))
    var callbackCalled = false
    BusSDK.instance.getCheapestFair(
        CheapestFareInput("HYD", "VIJ", LocalDate.parse("2022-01-10"))) {
      when (it) {
        is Ok -> Assert.fail("Expecting Error")
        is Err -> assertEquals(Err(CheapestFairError.JSON_PARSE_ERROR), it)
      }
      callbackCalled = true
    }
    await.until { callbackCalled }
  }
}
