package com.ixigo.sdk.analytics

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.analytics.FirebaseAnalytics
import com.ixigo.sdk.test.logging.FakeLog
import com.ixigo.sdk.test.logging.withFakeTree
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class FirebaseAnalyticsProviderTest {
  private lateinit var provider: FirebaseAnalyticsProvider
  private lateinit var context: Context
  private lateinit var mockFirebaseAnalytics: FirebaseAnalytics

  @Before
  fun setup() {
    context = getApplicationContext<Application>()
    provider = FirebaseAnalyticsProvider(context)
    mockFirebaseAnalytics = mock()
  }

  @Test
  fun `test provider is not enabled if Firebase is not present`() {
    withFakeTree {
      withoutFirebase {
        assertFalse(provider.enabled)
        assertLastLog(
            FakeLog(
                Log.ERROR,
                "Unable to instantiate Firebase Analytics. Did you include Firebase in the Host App?",
                NoClassDefFoundError::class.java))
      }
    }
  }

  @Test
  fun `test provider is enabled if Firebase is present`() {
    withFirebase { assertTrue(provider.enabled) }
  }

  @Test
  fun `test provider correctly forwards events to Firebase`() {
    withFirebase {
      val eventName = "eventName"
      val bundle = Bundle()
      provider.logEvent(eventName, bundle)
      verify(mockFirebaseAnalytics).logEvent(eq(eventName), same(bundle))
    }
  }

  @Test
  fun `test provider no-ops when logging events if Firebase is not available`() {
    withoutFirebase {
      val eventName = "eventName"
      val bundle = Bundle()
      provider.logEvent(eventName, bundle)
      verify(mockFirebaseAnalytics, never()).logEvent(eq(eventName), same(bundle))
    }
  }

  private fun withFirebase(block: () -> Unit) {
    mockStatic(FirebaseAnalytics::class.java).use { staticMock ->
      staticMock
          .`when`<Any> { FirebaseAnalytics.getInstance(context) }
          .thenReturn(mockFirebaseAnalytics)
      block()
    }
  }

  private fun withoutFirebase(block: () -> Unit) {
    mockStatic(FirebaseAnalytics::class.java).use { staticMock ->
      staticMock
          .`when`<Any> { FirebaseAnalytics.getInstance(context) }
          .thenThrow(NoClassDefFoundError())
      block()
    }
  }
}
