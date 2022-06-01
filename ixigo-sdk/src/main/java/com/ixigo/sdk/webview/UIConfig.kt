package com.ixigo.sdk.webview

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
sealed class BackNavigationMode {

  override fun equals(other: Any?) = other?.javaClass == javaClass
  override fun hashCode(): Int = javaClass.hashCode()

  @JsonClass(generateAdapter = true) @Keep class Enabled : BackNavigationMode()

  @JsonClass(generateAdapter = true) @Keep class Handler : BackNavigationMode()

  @JsonClass(generateAdapter = true) @Keep class Disabled : BackNavigationMode()
}

@Keep data class UIConfig(val backNavigationMode: BackNavigationMode)

interface UIConfigurable {
  fun configUI(uiConfig: UIConfig)
}
