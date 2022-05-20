package com.ixigo.sdk.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.test.R
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class LoadableViewTests {

  lateinit var activity: Activity
  lateinit var loadableView: LoadableViewContainer

  private val contentView: View
    get() = loadableView.contentView
  private val loadingView: View
    get() = loadableView.findViewById(R.id.progressView)
  private val errorView: View
    get() = loadableView.findViewById(R.id.errorView)

  @Mock lateinit var mockAnalyticsProvider: AnalyticsProvider

  @Rule fun rule(): MockitoRule = MockitoJUnit.rule()

  @Before
  fun setup() {
    initializeTestIxigoSDK(analyticsProvider = mockAnalyticsProvider)
    val app = ApplicationProvider.getApplicationContext<Application>()
    val intent =
        Intent(app, WebActivity::class.java).also {
          it.putExtra(
              WebViewFragment.INITIAL_PAGE_DATA_ARGS,
              InitialPageData(url = "https://www.ixigo.com/pwa"))
        }
    val scenario = launchActivity<WebActivity>(intent)
    scenario.onActivity {
      activity = it
      loadableView = it.findViewById(R.id.loadableView)
    }
  }

  @Test
  fun testInitialStateIsLoading() {
    assertEquals(Loading(referrer = "https://www.ixigo.com/pwa"), loadableView.status)
    assertEquals(GONE, contentView.visibility)
    assertEquals(GONE, errorView.visibility)
    assertEquals(VISIBLE, loadingView.visibility)
  }

  @Test
  fun `test LoadingView is shown when status is Loading`() {
    loadableView.status = Loading()
    assertEquals(GONE, contentView.visibility)
    assertEquals(GONE, errorView.visibility)
    assertEquals(VISIBLE, loadingView.visibility)
  }

  @Test
  fun `test ErrorView is shown when status is Error`() {
    loadableView.status = Failed()
    assertEquals(GONE, contentView.visibility)
    assertEquals(VISIBLE, errorView.visibility)
    assertEquals(GONE, loadingView.visibility)
  }

  @Test
  fun `test ProgressView has primaryColor as tint`() {
    val progressView: ProgressBar = loadableView.findViewById(R.id.progressView)
    assertEquals(
        ColorStateList.valueOf(IxigoSDK.instance.theme.primaryColor),
        progressView.indeterminateTintList)
  }

  @Test
  fun `test spinner time event is logged`() {
    Thread.sleep(500)
    loadableView.status = Loaded
    verify(mockAnalyticsProvider)
        .logEvent(
            event =
                argThat { event ->
                  event.name == "SpinnerTime" &&
                      event.referrer == "https://www.ixigo.com/pwa" &&
                      event.properties["value"]!!.toLong() > 500
                })
  }
}
