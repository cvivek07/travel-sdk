package com.ixigo.sdk.flights

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ixigo.sdk.IxigoSDK
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun IxigoSDK.flightsStartHome(context: Context) {
    val url = getUrl(mapOf(
        "page" to "FLIGHT_HOME"
    ))
    launchWebActivity(context, url)
}

fun IxigoSDK.flightsStartSearch(context: Context, searchData: FlightSearchData) {
    val url = getUrl(mapOf(
        "page" to "FLIGHT_LISTING",
        "orgn" to searchData.origin,
        "dstn" to searchData.destination,
        "departDate" to formatDate(searchData.departDate),
        "returnDate" to formatDate(searchData.returnDate),
        "adults" to searchData.passengerData.adults.toString(),
        "children" to searchData.passengerData.children.toString(),
        "infants" to searchData.passengerData.infants.toString(),
        "source" to searchData.source
    ))

    launchWebActivity(context, url)
}

private fun IxigoSDK.getUrl(properties: Map<String, String>): String {
    val builder = Uri.Builder().scheme("https").authority("www.ixigo.com").appendPath("pwa")
        .appendPath("initialpage")
        .appendQueryParameter("clientId", appInfo.clientId)
        .appendQueryParameter("apiKey", appInfo.apiKey)
        .appendQueryParameter("appVersion", appInfo.appVersion)
        .appendQueryParameter("deviceId", appInfo.deviceId)
        .appendQueryParameter("class", "e")
        .appendQueryParameter("languageCode", "en") // TODO
    for (property in properties) {
        builder.appendQueryParameter(property.key, property.value)
    }
    return builder.build().toString()
}

private fun IxigoSDK.launchWebActivity(context: Context, url: String) {
    val intent = Intent(context, WebActivity::class.java)
    intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData(url, getHeaders()))
    context.startActivity(intent)
}

private fun IxigoSDK.getHeaders(): Map<String, String> {
    val headers = mutableMapOf(
        "appVersion" to appInfo.appVersion,
        "clientId" to appInfo.clientId,
        "apiKey" to appInfo.apiKey,
        "deviceId" to appInfo.deviceId,
        "uuid" to appInfo.uuid
    )
    authProvider.authData?.let { headers["Authorization"] = it.token }
    return headers
}

private val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("ddMMyyyy")
private fun formatDate(date: LocalDate?): String = date?.format(formatter).orEmpty()

data class FlightSearchData(
    val origin: String,
    val destination: String,
    val departDate: LocalDate,
    val returnDate: LocalDate? = null,
    val passengerData: FlightPassengerData,
    val source: String
)

data class FlightPassengerData(val adults: Int, val children: Int, val infants: Int)

