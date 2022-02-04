package com.ixigo.sdk.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.test.R
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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

  @Before
  fun setup() {
    initializeTestIxigoSDK()
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
    assertEquals(Loading(), loadableView.status)
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
        ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.ixigosdk_primary_color)),
        progressView.indeterminateTintList)
  }
}
