@file:JvmName("FlightsFunnel")

package com.ixigo.sdk.flights

import com.ixigo.sdk.IxigoSDK
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun IxigoSDK.getFlightsSearchParams(page: String, searchData: FlightSearchData) =
    mapOf(
        "page" to page,
        "orgn" to searchData.origin,
        "dstn" to searchData.destination,
        "departDate" to formatDate(searchData.departDate),
        "returnDate" to formatDate(searchData.returnDate),
        "adults" to searchData.passengerData.adults.toString(),
        "children" to searchData.passengerData.children.toString(),
        "infants" to searchData.passengerData.infants.toString(),
        "class" to searchData.flightClass,
        "source" to searchData.source)

private val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("ddMMyyyy")

internal fun formatDate(date: LocalDate?): String = date?.format(formatter).orEmpty()

private fun parseDate(dateStr: String?): LocalDate? =
    try {
      LocalDate.parse(dateStr, formatter)
    } catch (_: Exception) {
      null
    }

data class FlightSearchData(
    val origin: String,
    val destination: String,
    val departDate: LocalDate,
    val returnDate: LocalDate? = null,
    val passengerData: FlightPassengerData,
    val flightClass: String,
    val source: String
) {
  constructor(
      origin: String,
      destination: String,
      departDateStr: String,
      returnDateStr: String?,
      passengerData: FlightPassengerData,
      flightClass: String,
      source: String
  ) : this(
      origin,
      destination,
      departDate = parseDate(departDateStr) ?: LocalDate.now().plusDays(1),
      returnDate = parseDate(returnDateStr),
      passengerData,
      flightClass,
      source)
}

data class FlightPassengerData(val adults: Int, val children: Int, val infants: Int)
