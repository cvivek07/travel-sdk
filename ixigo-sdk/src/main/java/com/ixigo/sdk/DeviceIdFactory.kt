package com.ixigo.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

@SuppressLint("HardwareIds")
class DeviceIdFactory(private val context: Context) {
  val deviceID: String by lazy {
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
  }
}
