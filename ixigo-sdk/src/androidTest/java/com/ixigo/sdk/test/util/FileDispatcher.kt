package com.ixigo.sdk.test.util

import java.nio.charset.StandardCharsets
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.apache.commons.io.IOUtils

class FileDispatcher : Dispatcher() {
  override fun dispatch(request: RecordedRequest): MockResponse {
    return try {
      val path = request.path ?: throw Error("Path is not present for request=$request")
      val resourceString = if (path.startsWith("/")) path.substring(1) else path
      val body =
          IOUtils.toString(
              javaClass.classLoader!!.getResourceAsStream(resourceString), StandardCharsets.UTF_8)
      MockResponse().setBody(body)
    } catch (e: Exception) {
      MockResponse().setHttp2ErrorCode(404)
    }
  }
}
