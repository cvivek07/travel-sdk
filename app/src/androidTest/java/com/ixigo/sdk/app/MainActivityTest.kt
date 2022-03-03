package com.ixigo.sdk.app

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ixigo.sdk.IxigoSDK
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun mainActivityTest() {
    val materialAutoCompleteTextView =
        onView(
            allOf(
                withText("ConfirmTkt"),
                childAtPosition(childAtPosition(withId(R.id.presetInput), 0), 1)))
    materialAutoCompleteTextView.perform(scrollTo(), click())

    val materialTextView = onData(anything())
      .inRoot(isPlatformPopup())
      .atPosition(1)
    materialTextView.perform(click())

    val materialButton =
        onView(
            allOf(
                withId(R.id.buttonFlightHome),
                withText("Flights Home"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("androidx.appcompat.widget.LinearLayoutCompat")), 5),
                    1)))
    materialButton.perform(scrollTo(), click())

    IdlingRegistry.getInstance().register(IxigoSDK.instance.uriIdlingResource)

//    Thread.sleep(5000)
//
    onWebView()
      .withElement(findElement(Locator.CSS_SELECTOR, "#content > div > div > div > div.home-widget-wrapper > div.featured-items-section.half-strips-cntnr > div:nth-child(1) > div.u-ib.text.strip-text")) // similar to onView(withId(...))
      .perform(webClick()) // Similar to perform(click())


    Thread.sleep(5000)

//    val linearLayout =
//        onView(
//            allOf(
//                withId(R.id.topExitBar),
//                childAtPosition(childAtPosition(withId(android.R.id.content), 0), 0),
//                isDisplayed()))
//    linearLayout.perform(click())
//
//    val appCompatButton2 =
//        onView(
//            allOf(
//                withId(android.R.id.button1),
//                withText("Go Back"),
//                childAtPosition(
//                    childAtPosition(withClassName(`is`("android.widget.ScrollView")), 0), 3)))
//    appCompatButton2.perform(scrollTo(), click())
  }

  private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description) {
        description.appendText("Child at position $position in parent ")
        parentMatcher.describeTo(description)
      }

      public override fun matchesSafely(view: View): Boolean {
        val parent = view.parent
        return parent is ViewGroup &&
            parentMatcher.matches(parent) &&
            view == parent.getChildAt(position)
      }
    }
  }
}
