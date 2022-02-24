package com.ixigo.sdk.app

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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

    val materialTextView =
        onData(anything())
            .inAdapterView(
                childAtPosition(
                    withClassName(`is`("android.widget.PopupWindow\$PopupBackgroundView")), 0))
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

    val appCompatButton =
        onView(
            allOf(
                withId(R.id.retryButton),
                withText("Retry"),
                childAtPosition(
                    allOf(withId(R.id.errorView), childAtPosition(withId(R.id.container), 1)), 2),
                isDisplayed()))
    appCompatButton.perform(click())

    val linearLayout =
        onView(
            allOf(
                withId(R.id.topExitBar),
                childAtPosition(childAtPosition(withId(android.R.id.content), 0), 0),
                isDisplayed()))
    linearLayout.perform(click())

    val appCompatButton2 =
        onView(
            allOf(
                withId(android.R.id.button1),
                withText("Go Back"),
                childAtPosition(
                    childAtPosition(withClassName(`is`("android.widget.ScrollView")), 0), 3)))
    appCompatButton2.perform(scrollTo(), click())
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
