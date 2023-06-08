package com.ixigo.sdk.util

import java.net.URL

fun isIxigoUrl(url: String): Boolean {
  return try {
    URL(url).host?.endsWith("ixigo.com") ?: false
  } catch (e: Exception) {
    false
  }
}
