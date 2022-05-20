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
    fun with(
        action: String,
        label: String? = null,
        referrer: String? = null,
        value: Long? = null
    ): Event {
      val properties = mutableMapOf<String, String>()
      label?.let { properties["label"] = it }
      value?.let { properties["value"] = it.toString() }
      return Event(name = action, properties = properties.toMap(), referrer = referrer)
    }
  }
}
