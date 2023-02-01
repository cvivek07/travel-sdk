package com.ixigo.sdk.common

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

internal fun Context.getMetaDataString(key: String): String {
  val ai: ApplicationInfo =
      this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)
  val bundle = ai.metaData

  if (!bundle.containsKey(key)) {
    throw RuntimeException("$key not found in app's manifest")
  }

  return bundle.get(key).toString()
}
