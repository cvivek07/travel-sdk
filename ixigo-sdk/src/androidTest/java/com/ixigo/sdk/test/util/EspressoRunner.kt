package com.ixigo.sdk.test.util

import android.os.Bundle
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner

class EspressoRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        super.onCreate(arguments)
    }

}