package com.ixigo.sdk.common

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import com.ixigo.sdk.IxigoSDK
import timber.log.Timber

class CustomChromeTabsHelper {

  fun openUrl(context: Context, url: String): Boolean {
    if (isCustomChromeTabsSupported(context)) {
      val builder = CustomTabsIntent.Builder()
      val colorInt = IxigoSDK.instance.theme.primaryColor
      val defaultColors = CustomTabColorSchemeParams.Builder().setToolbarColor(colorInt).build()
      builder.setDefaultColorSchemeParams(defaultColors)
      val customTabsIntent = builder.build()
      return try {
        customTabsIntent.launchUrl(context, Uri.parse(url))
        true
      } catch (e: Exception) {
        Timber.e(e, "Unable to url with Custom Chrome Tabs")
        false
      }
    } else {
      Timber.e("Unable to url with Custom Chrome Tabs. No compatible Apps found")
      return false
    }
  }

  private fun isCustomChromeTabsSupported(context: Context): Boolean {
    return getCustomTabsPackages(context).isNotEmpty()
  }

  /**
   * Returns a list of packages that support Custom Tabs. Taken as-is from
   * https://developer.chrome.com/docs/android/custom-tabs/integration-guide/
   */
  private fun getCustomTabsPackages(context: Context): List<ResolveInfo> {
    val pm = context.packageManager
    // Get default VIEW intent handler.
    val activityIntent =
        Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.fromParts("http", "", null))

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
    for (info in resolvedActivityList) {
      val serviceIntent = Intent()
      serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
      serviceIntent.setPackage(info.activityInfo.packageName)
      // Check if this package also resolves the Custom Tabs service.
      if (pm.resolveService(serviceIntent, 0) != null) {
        packagesSupportingCustomTabs.add(info)
      }
    }
    return packagesSupportingCustomTabs
  }
}
