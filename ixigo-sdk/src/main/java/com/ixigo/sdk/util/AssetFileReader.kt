package com.ixigo.sdk.util

import android.content.Context
import java.io.IOException

class AssetFileReader constructor(private val context: Context) {

  @Throws(IOException::class)
  fun readFile(fileName: String): String {
    return readAssetFileAsString(context, fileName)
  }

  @Throws(IOException::class)
  private fun readAssetFileAsString(context: Context, fileName: String): String {
    val assetManager = context.assets
    var fileContent = ""

    val inputStream = assetManager.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()

    fileContent = String(buffer, Charsets.UTF_8)
    return fileContent
  }
}
