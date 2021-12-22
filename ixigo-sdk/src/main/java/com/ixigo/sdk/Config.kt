package com.ixigo.sdk

data class Config(val apiBaseUrl: String) {

  fun createUrl(path: String): String = apiBaseUrl + path

  companion object {
    val ProdConfig = Config("https://www.ixigo.com/")
    fun StagingBuildConfig(buildId: String) = Config("https://${buildId}.ixigo.com/")
  }
}
