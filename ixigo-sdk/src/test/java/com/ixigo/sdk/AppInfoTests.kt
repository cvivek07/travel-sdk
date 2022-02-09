package com.ixigo.sdk

import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppInfoTests {

  @Mock lateinit var mockUUIDFactory: UUIDFactory
  @Mock lateinit var mockDeviceIdFactory: DeviceIdFactory

  @Test
  fun `test uuid and deviceId are replaced when default is used`() {
    val expectedUuid = UUID.randomUUID()
    val expectedDeviceId = "deviceId"
    Mockito.`when`(mockUUIDFactory.uuid).thenReturn(expectedUuid)
    Mockito.`when`(mockDeviceIdFactory.deviceID).thenReturn(expectedDeviceId)
    val appInfo =
        AppInfo(clientId = "clientId", apiKey = "apiKey", appVersion = 1, appName = "appName")
    val newAppInfo = appInfo.replaceDefaults(mockUUIDFactory, mockDeviceIdFactory)
    assertEquals(
        AppInfo(
            clientId = appInfo.clientId,
            apiKey = appInfo.apiKey,
            appVersion = appInfo.appVersion,
            appName = appInfo.appName,
            uuid = expectedUuid.toString(),
            deviceId = expectedDeviceId),
        newAppInfo)
  }

  @Test
  fun `test uuid and deviceId are NOT replaced when custom values are used`() {
    val customUuid = "myUuid"
    val customDeviceId = "myDeviceId"
    val appInfo =
        AppInfo(
            clientId = "clientId",
            apiKey = "apiKey",
            appVersion = 1,
            appName = "appName",
            uuid = customUuid,
            deviceId = customDeviceId)
    val newAppInfo = appInfo.replaceDefaults(mockUUIDFactory, mockDeviceIdFactory)
    assertEquals(
        AppInfo(
            clientId = appInfo.clientId,
            apiKey = appInfo.apiKey,
            appVersion = appInfo.appVersion,
            appName = appInfo.appName,
            uuid = customUuid,
            deviceId = customDeviceId),
        newAppInfo)
  }
}
