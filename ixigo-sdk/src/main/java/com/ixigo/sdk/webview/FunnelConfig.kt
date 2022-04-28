package com.ixigo.sdk.webview

import android.annotation.SuppressLint
import android.os.Parcelable
import com.ixigo.sdk.common.NoCoverage
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
@NoCoverage
data class FunnelConfig(
    val enableExitBar: Boolean? = null,
) : Parcelable
