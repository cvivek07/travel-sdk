package com.ixigo.sdk

import timber.log.Timber

fun IxigoSDK.setLoggingConfig(config: LoggingConfig) {
  if (config.enabled) {
    Timber.plant(
        object : Timber.DebugTree() {
          override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, "ixigosdk_$tag", message, t)
          }
        })
  } else {
    Timber.uprootAll()
  }
}

data class LoggingConfig(val enabled: Boolean)
