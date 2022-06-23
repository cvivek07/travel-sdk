package com.ixigo.sdk.ui

import android.content.Context
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.ixigo.sdk.R
import com.ixigo.sdk.util.ColorUtils.getMiddleColor
import com.ixigo.sdk.util.ColorUtils.isLightColor

/**
 * Theme is used to customize the UI of of certain chrome elements presented by the SDK eg: status
 * bar color, loader colors, spacing, etc...
 *
 * @property primaryColor
 */
data class Theme(val primaryColor: Int)

/**
 * This theme will read values from xml resources configuration
 *
 * @param context
 * @return
 */
fun defaultTheme(context: Context) =
    Theme(primaryColor = ContextCompat.getColor(context, R.color.ixigosdk_primary_color))

/**
 * Restricted class hierarchy for solid and gradient themes
 * @see GradientThemeColor
 * @see SolidThemeColor
 */
sealed class ThemeColor {
  abstract val isLight: Boolean
}

data class GradientThemeColor(val startColor: Int, val endColor: Int) : ThemeColor() {

  override val isLight: Boolean
    get() = isLightColor(getMiddleColor(startColor = startColor, endColor = endColor))
}

data class SolidThemeColor(val color: Int) : ThemeColor() {

  override val isLight: Boolean
    get() = isLightColor(color)
}

/** Model class for parsing themes from pwa meta headers */
@Keep data class SDKTheme(val gradientThemeColor: String?, val solidThemeColor: String)
