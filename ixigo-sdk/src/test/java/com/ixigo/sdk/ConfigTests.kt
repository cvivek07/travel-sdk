package com.ixigo.sdk

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigTests {

  @Test
  fun `test StagingConfig`() {
    val config = Config.StagingBuildConfig("myBuildId")
    assertEquals("https://myBuildId.ixigo.com/", config.apiBaseUrl)
  }
}
