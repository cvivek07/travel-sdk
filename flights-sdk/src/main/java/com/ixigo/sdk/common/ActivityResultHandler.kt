package com.ixigo.sdk.common

import android.content.Intent

/**
 * Common Interface to handle Activity Results
 */
interface ActivityResultHandler {
    /**
     * Handle result of an activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return Whether it was handled or not.
     * If the implementation of this method decides it will not handle it (eg: wrong requestCode), return false. Otherwise, return true
     */
    fun handle(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}
