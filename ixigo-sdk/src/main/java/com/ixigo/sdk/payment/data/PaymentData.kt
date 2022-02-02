package com.ixigo.sdk.payment.data

data class UpiApp(val appName: String, val appPackage: String)

data class GetAvailableUPIAppsResponse(val apps: List<UpiApp>)

data class GetAvailableUPIAppsInput(val orderId: String)
