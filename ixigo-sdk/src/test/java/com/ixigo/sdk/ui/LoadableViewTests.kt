package com.ixigo.sdk.ui

import android.app.Activity
import android.content.res.ColorStateList
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.test.R
import com.ixigo.sdk.test.initializeTestIxigoSDK
import com.ixigo.sdk.webview.WebActivity
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
    val scenario = launchActivity<WebActivity>().moveToState(Lifecycle.State.CREATED)
    scenario.onActivity {
      activity = it
      loadableView = it.findViewById(R.id.loadableView)
    }
  }

  @Test
  fun testInitialStateIsLoaded() {
    assertEquals(Loaded, loadableView.status)
    assertEquals(VISIBLE, contentView.visibility)
    assertEquals(GONE, errorView.visibility)
    assertEquals(GONE, loadingView.visibility)
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
