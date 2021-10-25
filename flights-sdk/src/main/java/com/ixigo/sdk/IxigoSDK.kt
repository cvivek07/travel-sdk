package com.ixigo.sdk

import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.EmptyAuthProvider
import com.ixigo.sdk.payment.EmptyPaymentProvider
import com.ixigo.sdk.payment.PaymentProvider
import kotlin.IllegalStateException

/**
 * This is the main entrypoint to interact with Ixigo SDK.
 *
 * All interactions should happen via its singleton object, `IxigoSDK.getInstance()`
 * Before using it, you need to call `IxigoSDK.init(...)` once when you start-up your Application
 *
 */
class IxigoSDK private constructor(val appInfo: AppInfo, val authProvider: AuthProvider = EmptyAuthProvider, val paymentProvider: PaymentProvider = EmptyPaymentProvider) {

    companion object {
        private var INSTANCE: IxigoSDK? = null

        /**
         * Initializes IxigoSDK with required parameters. This method needs to be called before accessing the singleton via [getInstance]
         *
         * Call this method when you initialize your Application. eg: `Application.onCreate`
         *
         * @param authProvider Delegates Authentication logic via this AuthProvider
         * @param paymentProvider Delegates Payment logic via this PaymentProvider
         * @param appInfo The AppInfo
         */
        @JvmStatic
        fun init(authProvider: AuthProvider, paymentProvider: PaymentProvider, appInfo: AppInfo) {
            if (INSTANCE != null) {
                throw IllegalStateException("IxigoSDK has already been initialized")
            }
            INSTANCE = IxigoSDK(appInfo, authProvider, paymentProvider)
        }

        /**
         * Returns IxigoSDK singleton.
         *
         * Will throw an Exception if it has not been initialized yet by calling [init]
         *
         * @return IxigoSDK singleton
         */
        @JvmStatic
        fun getInstance(): IxigoSDK {
            return INSTANCE
                ?: throw IllegalStateException("IxigoSDK has not been initialized. Call `IxigoSDK.init()` to initialize it.")
        }

        /**
         * Resets IxigoSDK singleton. Used only for tests
         *
         */
        internal fun clearInstance() {
            INSTANCE = null
        }
    }
}

/**
 * Information about the host App
 *
 * @property clientId
 * @property apiKey
 * @property appVersion
 * @property deviceId
 * @property uuid
 */
data class AppInfo(val clientId: String, val apiKey: String, val appVersion: String, val deviceId: String, val uuid: String)
