package com.ixigo.sdk.common

sealed class Result<out T> {
    fun onSuccess(block: (T) -> Unit) {
        when (this) {
            is Ok -> block(value)
            else -> Unit
        }
    }

    fun <S>mapBoth(success: (T) -> S, failure: (Error) -> S): S {
        return when (this) {
            is Ok -> success(value)
            is Err -> failure(value)
        }
    }
}

class Ok<T>(val value: T): Result<T>()
class Err(val value: Error): Result<Nothing>()