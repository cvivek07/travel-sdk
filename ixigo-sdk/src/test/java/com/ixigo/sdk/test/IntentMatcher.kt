package com.ixigo.sdk.test

import android.app.Activity
import android.content.Intent
import com.ixigo.sdk.test.TestData.FakeAppInfo
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.MatcherAssert
import org.robolectric.Shadows

class IntentMatcher(val intent: Intent) : BaseMatcher<Intent>() {
  override fun describeTo(description: Description?) {
    description?.appendText(describeIntent(intent))
  }

  override fun describeMismatch(item: Any?, description: Description?) {
    description?.appendText(describeIntent(item as Intent))
  }

  override fun matches(item: Any?): Boolean {
    val itemIntent = item as Intent? ?: return false
    return itemIntent.filterEquals(intent) &&
        getInitialPageData(itemIntent) == getInitialPageData(intent)
  }

  private fun getInitialPageData(intent: Intent): InitialPageData =
      intent.getParcelableExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS)!!

  private fun describeIntent(intent: Intent): String {
    return intent.extras.toString()
  }
}

fun assertLaunchedIntent(
    activity: Activity,
    url: String,
    expectedHeaders: Map<String, String> = defaultIntentHeaders
) {
  val intent = Intent(activity, WebActivity::class.java)
  intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, expectedHeaders))
  val shadowActivity = Shadows.shadowOf(activity)
  val nextIntent = shadowActivity.nextStartedActivity
  MatcherAssert.assertThat(nextIntent, IntentMatcher(intent))
}

val defaultIntentHeaders: Map<String, String> =
    with(FakeAppInfo) {
      mutableMapOf(
          "appVersion" to appVersionString,
          "clientId" to clientId,
          "apiKey" to apiKey,
          "deviceId" to deviceId,
          "uuid" to uuid,
      )
    }
