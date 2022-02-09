package com.ixigo.sdk

import android.net.Uri

data class Config(
    val apiBaseUrl: String = "https://www.ixigo.com/",
    val enableExitBar: Boolean = true
) {

  fun createUrl(path: String?, parameters: Map<String, String> = mapOf()): String {
    val builder = Uri.parse(apiBaseUrl).buildUpon()
    path?.let { builder.appendEncodedPath(it) }
    parameters.forEach { builder.appendQueryParameter(it.key, it.value) }
    return builder.toString()
  }

  companion object {
    val ProdConfig = Config()
    fun StagingBuildConfig(buildId: String) = Config("https://${buildId}.ixigo.com/")
  }
}
