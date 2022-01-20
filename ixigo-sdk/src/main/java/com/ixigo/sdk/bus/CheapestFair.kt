package com.ixigo.sdk.bus

import java.time.LocalDate

fun BusSDK.cheapestFare() {}

data class CheapestFareInput(val origin: String, val destination: String, val date: LocalDate)

data class CheapestFareOutput(val minFare: String)
