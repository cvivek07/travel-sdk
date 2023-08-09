package com.ixigo.sdk.webview

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
@Keep
sealed class BackNavigationMode {

  override fun equals(other: Any?) = other?.javaClass == javaClass
  override fun hashCode(): Int = javaClass.hashCode()

  @JsonClass(generateAdapter = false) @Keep class Enabled : BackNavigationMode()

  @JsonClass(generateAdapter = false) @Keep class Handler : BackNavigationMode()

  @JsonClass(generateAdapter = false) @Keep class Disabled : BackNavigationMode()
}

@Keep data class UIConfig(val backNavigationMode: BackNavigationMode)

interface UIConfigurable {
  fun configUI(uiConfig: UIConfig)
}
