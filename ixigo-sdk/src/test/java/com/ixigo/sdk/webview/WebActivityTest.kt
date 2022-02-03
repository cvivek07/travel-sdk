package com.ixigo.sdk.webview

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.R
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.test.initializeTestIxigoSDK
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class WebActivityTest {

  private val initialPageData =
      InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))

  @Before
  fun setup() {
    initializeTestIxigoSDK()
  }

  @Test
  fun `test calling onQuit finishes activity`() {
    withWebActivity { activity ->
      activity.onQuit()
      val webViewFragment = activity.supportFragmentManager.fragments[0] as WebViewFragment
      assertSame(activity, webViewFragment.delegate)
      assertEquals(
          initialPageData,
          webViewFragment
              .requireArguments()
              .getParcelable<InitialPageData>(WebViewFragment.INITIAL_PAGE_DATA_ARGS))
      assertTrue(activity.isFinishing)
    }
  }

  @Test
  fun `test with light statusBar color`() {
    assertStatusBar(true)
  }

  // `es` configures a dark status bar color
  @Test
  @Config(qualifiers = "es")
  fun `test with dark statusBar color`() {
    assertStatusBar(false)
  }

  @Test
  fun `test onActivityResult forwards call to webFragment`() {
    val mockPartnerTokenProvider: ResultPartnerTokenProvider = mock()
    val requestCode = 1001
    val responseCode = 200
    val intent: Intent = mock()

    initializeTestIxigoSDK(partnerTokenProvider = mockPartnerTokenProvider)
    withWebActivity { activity ->
      val onActivityResultMethod =
          activity.javaClass.getDeclaredMethod(
              "onActivityResult", Integer.TYPE, Integer.TYPE, Intent::class.java)
      onActivityResultMethod.isAccessible = true
      onActivityResultMethod.invoke(activity, requestCode, responseCode, intent)
      verify(mockPartnerTokenProvider).handle(requestCode, responseCode, intent)
    }
  }

  private fun assertStatusBar(expectedLight: Boolean) {
    withWebActivity { activity ->
      val rootView = activity.findViewById<View>(android.R.id.content).rootView
      assertEquals(
          expectedLight,
          WindowInsetsControllerCompat(activity.window, rootView).isAppearanceLightStatusBars)
      @Suppress("DEPRECATION")
      assertEquals(
          activity.resources.getColor(R.color.ixigosdk_primary_color),
          activity.window.statusBarColor)
    }
  }

  private fun withWebActivity(activityAction: ActivityScenario.ActivityAction<WebActivity>) {
    val app = getApplicationContext<Application>()
    val intent =
        Intent(app, WebActivity::class.java).also {
          it.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
        }
    val scenario = launchActivity<WebActivity>(intent)
    scenario.onActivity(activityAction)
  }
}

interface ResultPartnerTokenProvider : PartnerTokenProvider, ActivityResultHandler
