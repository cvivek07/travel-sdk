package com.ixigo.sdk.webview

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
sealed class BackNavigationMode {

  override fun equals(other: Any?) = other?.javaClass == javaClass
  override fun hashCode(): Int = javaClass.hashCode()

  @JsonClass(generateAdapter = true) class Enabled : BackNavigationMode()

  @JsonClass(generateAdapter = true) class Handler : BackNavigationMode()

  @JsonClass(generateAdapter = true) class Disabled : BackNavigationMode()
}

@Keep data class UIConfig(val backNavigationMode: BackNavigationMode)

interface UIConfigurable {
  fun configUI(uiConfig: UIConfig)
}
