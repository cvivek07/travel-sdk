package com.ixigo.sdk

import com.ixigo.sdk.auth.AuthProvider
import kotlin.IllegalStateException

class IxigoSDK private constructor(val authProvider: AuthProvider, val appInfo: AppInfo) {

    companion object {
        private var INSTANCE: IxigoSDK? = null

        fun init(authProvider: AuthProvider, appInfo: AppInfo) {
            if (INSTANCE != null) {
                throw IllegalStateException("IxigoSDK has already been initialized")
            }
            INSTANCE = IxigoSDK(authProvider, appInfo)
        }

        fun getInstance(): IxigoSDK {
            return INSTANCE
                ?: throw IllegalStateException("IxigoSDK has not been initialized. Call `IxigoSDK.init()` to initialize it.")
        }

        internal fun clearInstance() {
            INSTANCE = null
        }
    }
}

data class AppInfo(val clientId: String, val apiKey: String, val appVersion: String, val deviceId: String, val uuid: String)
