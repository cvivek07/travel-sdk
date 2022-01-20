package com.ixigo.sdk.bus

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BusSearchData(
    val sourceName: String,
    val sourceId: Int,
    val destinationName: String,
    val destinationId: Int,
    val date: LocalDate
) {
  companion object {
    internal val formatter: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
  }
  val dateString: String
    get() = formatter.format(date)
}
