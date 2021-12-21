package com.ixigo.sdk.common

/**
 * Result class to return values from different APIs
 *
 * This replaces [Kotlin Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) as it
 * requires kotlin 1.5 to return as a type
 *
 * @param T The type of a successful response
 */
sealed class Result<out T> {
  val isSuccess: Boolean by lazy {
    when (this) {
      is Ok -> true
      else -> false
    }
  }

  fun onSuccess(block: (T) -> Unit) {
    when (this) {
      is Ok -> block(value)
      else -> Unit
    }
  }

  fun <S> mapBoth(success: (T) -> S, failure: (Throwable) -> S): S {
    return when (this) {
      is Ok -> success(value)
      is Err -> failure(value)
    }
  }

  /**
   * Used for logging
   *
   * @return "Success" or "Error", depending on the value of the Result
   */
  fun simpleString(): String {
    return when (this) {
      is Ok -> "Success"
      is Err -> "Error"
    }
  }
}

class Ok<T>(val value: T) : Result<T>() {
  override fun equals(other: Any?): Boolean {
    @Suppress("UNCHECKED_CAST") val otherOk = other as? Ok<T> ?: return false
    return otherOk.value == value
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

class Err(val value: Throwable) : Result<Nothing>() {
  override fun equals(other: Any?): Boolean {
    val otherErr = other as? Err ?: return false
    return otherErr.value::class == value::class
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
