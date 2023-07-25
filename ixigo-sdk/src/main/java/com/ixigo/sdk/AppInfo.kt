package com.ixigo.sdk

import androidx.annotation.Keep

/**
 * Information about the host App
 *
 * @property clientId
 * @property apiKey
 * @property appVersion
 * @property appName App Name will be this displayed in certain dialogs while using Ixigo SDK
 * @property deviceId Device Id should be constant through the use of your App for a specific user.
 * Do not set unless you have a specific use.
 * @property uuid UUID uniquely identifies a specific user. Do not set unless you have a specific
 * use.
 */
@Keep
data class AppInfo
@JvmOverloads
constructor(
    val clientId: String,
    val apiKey: String,
    val appVersion: Long,
    val appName: String,
    val deviceId: String = defaultDeviceId,
    val uuid: String = defaultUuid
) {
  val appVersionString
    get() = appVersion.toString()

  internal fun replaceDefaults(
      uuidFactory: UUIDFactory,
      deviceIdFactory: DeviceIdFactory
  ): AppInfo {
    val newUuid =
        if (uuid == defaultUuid) {
          uuidFactory.uuid.toString()
        } else {
          uuid
        }
    val newDeviceId =
        if (deviceId == defaultDeviceId) {
          deviceIdFactory.deviceID
        } else {
          deviceId
        }
    return AppInfo(
        clientId = clientId,
        apiKey = apiKey,
        appVersion = appVersion,
        appName = appName,
        uuid = newUuid,
        deviceId = newDeviceId)
  }
}

private const val defaultDeviceId = "defaultDeviceId"
private const val defaultUuid = "defaultUuid"
