package com.ixigo.sdk.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.Error

sealed class Result<out S: Parcelable> : Parcelable

@Parcelize
data class Success<T:Parcelable>(val value: T): Result<T>()
@Parcelize
data class Failure(val error: Error): Result<Nothing>()
