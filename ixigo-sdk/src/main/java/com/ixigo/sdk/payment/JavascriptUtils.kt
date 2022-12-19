package com.ixigo.sdk.payment

fun escapeSpecialCharacters(param: String): String {
  val chars = param.toCharArray()
  val sb = StringBuilder()
  chars.forEach {
    when (it) {
      '\\' -> sb.append("\\\\")
      '\n' -> sb.append("\\n")
      '\r' -> sb.append("\\r")
      '\b' -> sb.append("\\b")
      '\t' -> sb.append("\\t")
      '\'' -> sb.append("\\u0027")
      else -> sb.append(it)
    }
  }
  return sb.toString()
}
