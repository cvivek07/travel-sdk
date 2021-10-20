package com.ixigo.sdk

import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.payment.EmptyPaymentProvider
import com.ixigo.sdk.payment.PaymentProvider
import kotlin.IllegalStateException

class IxigoSDK private constructor(val appInfo: AppInfo, val authProvider: AuthProvider = EmptyAuthProvider, val paymentProvider: PaymentProvider = EmptyPaymentProvider) {

    companion object {
        private var INSTANCE: IxigoSDK? = null

        fun init(authProvider: AuthProvider, paymentProvider: PaymentProvider, appInfo: AppInfo) {
            if (INSTANCE != null) {
                throw IllegalStateException("IxigoSDK has already been initialized")
            }
            INSTANCE = IxigoSDK(appInfo, authProvider, paymentProvider)
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
