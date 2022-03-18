package com.ixigo.sdk.webview

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.R
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.common.ActivityResultHandler
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.ui.defaultTheme
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
    initializeTestIxigoSDK(
        config = com.ixigo.sdk.Config(enableExitBar = false),
        theme = defaultTheme(getApplicationContext()))
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

  @Test
  fun `test that confirming ExitTopBar Dialog dismisses activity`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = true))
    withWebActivity { activity ->
      onView(withId(R.id.topExitBar)).perform(click())
      onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
      assertTrue(activity.isFinishing)
    }
  }

  @Test
  fun `test that cancelling ExitTopBar Dialog does NOT call onQuit on delegate`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = true))
    withWebActivity { activity ->
      onView(withId(R.id.topExitBar)).perform(click())
      onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click())
      assertFalse(activity.isFinishing)
    }
  }

  @Test
  fun `test statusBar color is the same as exitBar when exitBar is enabled`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = true))
    withWebActivity { activity ->
      assertEquals(VISIBLE, activity.binding.topExitBar.visibility)
      assertEquals(
          getColor(activity, R.color.exit_top_nav_bar_color), activity.window.statusBarColor)
    }
  }

  @Test
  fun `test exitBar is enabled when config says so`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = false))
    withWebActivity(FunnelConfig(enableExitBar = true)) { activity ->
      assertEquals(VISIBLE, activity.binding.topExitBar.visibility)
    }
  }

  @Test
  fun `test exitBar is disabled when config says so`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = true))
    withWebActivity(FunnelConfig(enableExitBar = false)) { activity ->
      assertEquals(GONE, activity.binding.topExitBar.visibility)
    }
  }

  @Test
  fun `test statusBar color is updated from delegate if status bar is disabled`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = false))
    withWebActivity { activity ->
      val color = Color.parseColor("#00FF00")
      activity.updateStatusBarColor(color)
      assertEquals(color, activity.window.statusBarColor)
    }
  }

  @Test
  fun `test statusBar color is NOT updated from delegate if status bar is enabled`() {
    initializeTestIxigoSDK(config = com.ixigo.sdk.Config(enableExitBar = true))
    withWebActivity { activity ->
      activity.updateStatusBarColor(Color.parseColor("#00FF00"))
      assertEquals(
          getColor(activity, R.color.exit_top_nav_bar_color), activity.window.statusBarColor)
    }
  }

  private fun assertStatusBar(expectedLight: Boolean) {
    withWebActivity { activity ->
      val rootView = activity.findViewById<View>(android.R.id.content).rootView
      assertEquals(
          expectedLight,
          WindowInsetsControllerCompat(activity.window, rootView).isAppearanceLightStatusBars)
      @Suppress("DEPRECATION")
      assertEquals(IxigoSDK.instance.theme.primaryColor, activity.window.statusBarColor)
    }
  }

  private fun withWebActivity(
      config: FunnelConfig? = null,
      activityAction: ActivityScenario.ActivityAction<WebActivity>
  ) {
    val app = getApplicationContext<Application>()
    val intent: Intent =
        Intent(app, WebActivity::class.java).apply {
          putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, initialPageData)
          config?.let { putExtra(WebViewFragment.CONFIG, it) }
        }
    val scenario = launchActivity<WebActivity>(intent)
    scenario.onActivity(activityAction)
  }
}

interface ResultPartnerTokenProvider : PartnerTokenProvider, ActivityResultHandler
