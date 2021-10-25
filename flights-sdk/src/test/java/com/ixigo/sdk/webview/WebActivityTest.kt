package com.ixigo.sdk.webview

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

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