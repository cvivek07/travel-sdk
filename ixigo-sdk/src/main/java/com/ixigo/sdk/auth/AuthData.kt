package com.ixigo.sdk.auth

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize @SuppressLint("ParcelCreator") data class AuthData(val token: String) : Parcelable
