package com.ixigo.sdk.bus

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ixigo.sdk.BuildConfig
import com.ixigo.sdk.analytics.AnalyticsProvider
import com.ixigo.sdk.analytics.Event
import java.lang.IllegalStateException
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class BusSDKTests {

  @Before
  fun setup() {
    BusSDK.clearInstance()
  }

  @Test
  fun `test init sends correct analytics event`() {
    val analyticsProvider: AnalyticsProvider = mock()
    val context: Context = mock()
    BusSDK.internalInit(context, analyticsProvider)
    verify(analyticsProvider)
        .logEvent(
            Event(
                name = "sdkInit",
                properties = mapOf("sdk" to "bus", "sdkVersion" to BuildConfig.SDK_VERSION)))
  }

  @Test(expected = IllegalStateException::class)
  fun `test calling init twice throws an exception`() {
    val analyticsProvider: AnalyticsProvider = mock()
    val context: Context = mock()
    BusSDK.internalInit(context, analyticsProvider)
    BusSDK.internalInit(context, analyticsProvider)
  }

  @Test
  fun `test bus home`() {
    // TODO: Redo after implementation of `BusSDK.busHome` is done
    BusSDK.internalInit(mock(), mock())
    BusSDK.getInstance().busHome()
  }

  @Test
  fun `test bus multimodel fragment`() {
    // TODO: Redo after implementation of `BusSDK.busHome` is done
    BusSDK.internalInit(mock(), mock())
    val fragment =
        BusSDK.getInstance()
            .busMultiModelFragment(
                BusSearchData(origin = "originValue", destination = "destinationValue"))
    assertNotNull(fragment)
  }
}
