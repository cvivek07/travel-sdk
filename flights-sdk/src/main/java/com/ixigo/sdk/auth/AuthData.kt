package com.ixigo.sdk.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthData(val token: String) : Parcelable