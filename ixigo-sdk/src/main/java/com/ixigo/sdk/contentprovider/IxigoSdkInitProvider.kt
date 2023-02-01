package com.ixigo.sdk.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import com.ixigo.sdk.common.getMetaDataString
import com.ixigo.sdk.payment.PaymentSDK

class IxigoSdkInitProvider : ContentProvider() {

  override fun attachInfo(context: Context, info: ProviderInfo) {
    super.attachInfo(context, info)
    PaymentSDK.bootUp(context, juspayClientId = context.getMetaDataString(key = "juspay-client-id"))
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
