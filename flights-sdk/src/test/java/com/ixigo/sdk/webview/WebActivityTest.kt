package com.ixigo.sdk.webview

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.auth.test.FakeAuthProvider
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.payment.*
import com.ixigo.sdk.webview.WebActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowWebView

@RunWith(AndroidJUnit4::class)
class WebActivityTest {

    private val initialPageData =
        InitialPageData("https://www.ixigo.com", mapOf("header1" to "header1Value"))

    @Test
    fun `test calling quit finishes activity`() {
        val app = getApplicationContext<Application>()
        val intent = Intent(app, WebActivity::class.java).also {
            it.putExtra(
                WebViewFragment.INITIAL_PAGE_DATA_ARGS,
                initialPageData
            )
        }
        val scenario = launchActivity<WebActivity>(intent)
        scenario.onActivity { activity ->
            activity.quit()
            val webViewFragment = activity.supportFragmentManager.fragments[0] as WebViewFragment
            assertSame(activity, webViewFragment.delegate)
            assertEquals(initialPageData, webViewFragment.requireArguments().getParcelable<InitialPageData>(WebViewFragment.INITIAL_PAGE_DATA_ARGS))
            assertTrue(activity.isFinishing)
        }
    }
}