package com.ixigo.sdk.webview

import androidx.annotation.Keep

@Keep data class UIConfig(val enableBackNavigation: Boolean = true)

interface UIConfigurable {
  fun configUI(uiConfig: UIConfig)
}
