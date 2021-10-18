package com.ixigo.sdk.common

sealed class Progress<out T>
object NotStarted: Progress<Nothing>()
class InProgress(val progress: Double): Progress<Nothing>()
class Done<T>(val value: T): Progress<T>()
