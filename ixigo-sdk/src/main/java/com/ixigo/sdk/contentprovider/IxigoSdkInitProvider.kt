package com.ixigo.sdk.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import `in`.juspay.services.HyperServices

class IxigoSdkInitProvider : ContentProvider() {

  override fun attachInfo(context: Context, info: ProviderInfo) {
    super.attachInfo(context, info)
    val ai: ApplicationInfo =
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    val bundle = ai.metaData

    if (!bundle.containsKey("juspay-client-id")) {
      throw RuntimeException("Juspay client Id not found in app's manifest")
    }

    val clientId = bundle.get("juspay-client-id").toString()
    HyperServices.preFetch(context, clientId)
  }

  override fun onCreate(): Boolean {
    return false
  }

  override fun query(
      uri: Uri,
      projection: Array<out String>?,
      selection: String?,
      selectionArgs: Array<out String>?,
      sortOrder: String?
  ): Cursor? {
    return null
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    return null
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
    return 0
  }

  override fun update(
      uri: Uri,
      values: ContentValues?,
      selection: String?,
      selectionArgs: Array<out String>?
  ): Int {
    return 0
  }
}
