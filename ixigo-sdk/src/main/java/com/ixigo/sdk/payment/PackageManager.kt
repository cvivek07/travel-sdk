package com.ixigo.sdk.payment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import timber.log.Timber

/** Helper class to check if a particular app is installed in user's device */
class PackageManager(private val applicationContext: Context) {



  fun isPhonePeUpiAvailable(): Boolean {
    val phonePePackageNameProduction = "com.phonepe.app" // Production Environment
    val uri: Uri = Uri.parse(String.format("%s://%s", "upi", "pay"))
    val upiUriIntent = Intent()
    upiUriIntent.data = uri
    val packageManager = applicationContext.packageManager
    val resolveInfoList: List<ResolveInfo> =
        packageManager.queryIntentActivities(upiUriIntent, PackageManager.MATCH_DEFAULT_ONLY)
    for (resolveInfo in resolveInfoList) {
      val packageName = resolveInfo.activityInfo.packageName
      if (packageName != null &&
          packageName.isNotEmpty() &&
          phonePePackageNameProduction == packageName) {
        return true
      }
    }
    return false
  }

  /** Get PhonePe Version code */
  fun extractPhonePeVersionCode(): Long {
    val packageInfo: PackageInfo?
    var phonePeVersionCode = -1L
    try {
      packageInfo =
          applicationContext.packageManager.getPackageInfo(
              PHONEPE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
      phonePeVersionCode =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
          } else {
            packageInfo.versionCode.toLong()
          }
    } catch (e: PackageManager.NameNotFoundException) {
      Timber.e(e)
    }
    return phonePeVersionCode
  }

  /** Check if Cred app is installed on device */
  fun isCredAppInstalled(): Boolean {
    try {
      val credpackage = CRED_PACKAGE_NAME
      val pm: PackageManager =
          applicationContext.packageManager.apply {
            getPackageInfo(credpackage, PackageManager.GET_ACTIVITIES)
          }
      return pm.getApplicationInfo(credpackage, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return false
  }

  companion object {
    const val CRED_PACKAGE_NAME = "com.dreamplug.androidapp"
    const val PHONEPE_PACKAGE_NAME = "com.phonepe.app"
    const val REQUEST_CODE_PHONEPE_APP = 101
    const val REQUEST_CODE_GPAY_APP = 102
    const val REQUEST_CODE_UPI_APP = 103
  }
}
