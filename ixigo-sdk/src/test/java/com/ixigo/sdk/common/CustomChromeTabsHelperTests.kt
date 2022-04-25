package com.ixigo.sdk.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.payment.PaymentSDK
import com.ixigo.sdk.test.initializeTestIxigoSDK
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowPackageManager

@RunWith(AndroidJUnit4::class)
class CustomChromeTabsHelperTests {

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  private lateinit var scenario: ActivityScenario<FragmentActivity>
  private lateinit var activity: Activity

  @Mock private lateinit var mockContext: Context

  @Mock private lateinit var mockPackageManager: PackageManager

  private lateinit var customChromeTabsHelper: CustomChromeTabsHelper

  private val expectedIntent =
      Intent()
          .setAction(Intent.ACTION_VIEW)
          .addCategory(Intent.CATEGORY_BROWSABLE)
          .setData(Uri.fromParts("http", "", null))

  private lateinit var expectedLaunchedIntent: CustomTabsIntent

  @Before
  fun setup() {
    scenario = launchActivity()
    scenario.onActivity { activity = it }
    customChromeTabsHelper = CustomChromeTabsHelper()
    whenever(mockContext.packageManager).thenReturn(mockPackageManager)
    initializeTestIxigoSDK()

    expectedLaunchedIntent =
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(IxigoSDK.instance.theme.primaryColor)
                    .build())
            .build()
  }

  @After
  fun teardown() {
    IxigoSDK.clearInstance()
    PaymentSDK.clearInstance()
  }

  @Test
  fun `test openUrl returns false if no available Apps`() {
    val ret = customChromeTabsHelper.openUrl(activity, "https://www.booking.com")
    assertFalse(ret)
    assertNull(shadowOf(activity).nextStartedActivity)
  }

  @Test
  fun `test openUrl returns true if there is at least 1 available App`() {
    val activityInfo = ActivityInfo()
    activityInfo.packageName = "com.google.chrome"
    val resolveInfo = ResolveInfo()
    resolveInfo.serviceInfo = ServiceInfo()
    resolveInfo.activityInfo = activityInfo

    val expectedServiceIntent =
        Intent().setAction(ACTION_CUSTOM_TABS_CONNECTION).setPackage(activityInfo.packageName)
    val shadowPackageManager: ShadowPackageManager = shadowOf(activity.packageManager)
    shadowPackageManager.addResolveInfoForIntent(expectedIntent, resolveInfo)
    shadowPackageManager.addResolveInfoForIntent(expectedServiceIntent, resolveInfo)

    val ret = customChromeTabsHelper.openUrl(activity, "https://www.booking.com")

    assertTrue(ret)
    val expectedLaunchedIntent =
        Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://www.booking.com"))

    val shadowActivity: ShadowActivity = shadowOf(activity)
    val nextActivity = shadowActivity.nextStartedActivity
    assertTrue(expectedLaunchedIntent.filterEquals(nextActivity))
    assertEquals(
        IxigoSDK.instance.theme.primaryColor,
        nextActivity.getIntExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0))
  }
}
