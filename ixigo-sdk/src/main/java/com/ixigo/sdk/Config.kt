package com.ixigo.sdk

import android.net.Uri

/**
 * This class holds functionality configuration for the SDK eg: url endpoints
 *
 * @property apiBaseUrl main url to hit ixigo endpoints eg: `https://www.ixigo.com`
 * @property enableExitBar whether or not to display a bar at the top of the screen to exit the
 * current SDK flow
 */
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
