package com.ixigo.sdk.test

import android.content.Intent
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class IntentMatcher(val intent: Intent) : BaseMatcher<Intent>() {
  override fun describeTo(description: Description?) {}

  override fun matches(item: Any?): Boolean {
    val itemIntent = item as Intent? ?: return false
    return itemIntent.filterEquals(intent) &&
        getInitialPageData(itemIntent) == getInitialPageData(intent)
  }

  private fun getInitialPageData(intent: Intent): InitialPageData =
      intent.getParcelableExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS)!!
}
