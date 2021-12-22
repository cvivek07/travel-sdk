package com.ixigo.sdk.analytics

interface AnalyticsProvider {
  fun logEvent(event: Event)
}

data class Event(
    val category: String = "action",
    val action: String,
    val value: Long? = null,
    val label: String? = null,
    val dimensions: Map<EventDimension, String> = mapOf()
)

enum class EventDimension(val index: Int) {
  CLIENT_ID(1),
  SDK_VERSION(2)
}
