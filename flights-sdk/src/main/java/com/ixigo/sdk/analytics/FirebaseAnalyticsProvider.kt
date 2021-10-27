package com.ixigo.sdk.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

internal class FirebaseAnalyticsProvider(val context: Context): AnalyticsProvider {

    private val firebaseAnalytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseAnalytics.getInstance(context)
        } catch (e: Error) {
            Timber.e(e, "Unable to instantiate Firebase Analytics. Did you include Firebase in the Host App?")
            null
        }
    }

    override val enabled: Boolean
        get() = firebaseAnalytics != null

    override fun logEvent(name: String, params: Bundle) {
        firebaseAnalytics?.logEvent(name, params)
    }
}