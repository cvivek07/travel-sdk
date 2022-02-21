@file:JvmName("FlightsFunnel")

package com.ixigo.sdk.flights

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.analytics.Event
import com.ixigo.sdk.webview.FunnelConfig
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebViewFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun IxigoSDK.flightsStartHome(context: Context) {
  val url = getUrl(mapOf("page" to "FLIGHT_HOME"))
  launchWebActivity(context, url)
  analyticsProvider.logEvent(Event.with(action = "flightsStartHome"))
}

fun IxigoSDK.flightsStartTrips(context: Context) {
  val url = getUrl(mapOf("page" to "FLIGHT_TRIPS"))
  launchWebActivity(context, url)
  analyticsProvider.logEvent(Event.with(action = "flightsStartTrips"))
}

fun IxigoSDK.flightsTripsFragment(): Fragment {
  val arguments =
      Bundle().apply {
        val url = getUrl(mapOf("page" to "FLIGHT_TRIPS"))
        putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
        putParcelable(WebViewFragment.CONFIG, FunnelConfig(enableExitBar = false))
      }

  return WebViewFragment().apply { this.arguments = arguments }
}

fun IxigoSDK.flightsStartSearch(context: Context, searchData: FlightSearchData) {
  val url = getUrl(getFlightsSearchParams("FLIGHT_LISTING", searchData))
  launchWebActivity(context, url)

  analyticsProvider.logEvent(Event.with(action = "flightsStartSearch"))
}

fun IxigoSDK.flightsMultiModelFragment(searchData: FlightSearchData): Fragment {
  val arguments =
      Bundle().apply {
        val url = getUrl(getFlightsSearchParams("FLIGHT_LISTING_MULTI_MODEL", searchData))
        putParcelable(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders(url)))
        putParcelable(WebViewFragment.CONFIG, FunnelConfig(enableExitBar = false))
      }

  return WebViewFragment().apply { this.arguments = arguments }
}

private fun IxigoSDK.getFlightsSearchParams(page: String, searchData: FlightSearchData) =
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
