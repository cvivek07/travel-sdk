package com.ixigo.sdk.analytics

interface AnalyticsProvider {
  fun logEvent(event: Event)
}

data class Event(
    val name: String,
    val properties: Map<String, String> = mapOf(),
    val referrer: String? = null
) {
  companion object {
    fun with(action: String, label: String? = null, referrer: String? = null): Event =
        Event(
            name = action,
            properties = label?.let { mapOf("label" to it) } ?: mapOf(),
            referrer = referrer)
  }
}
