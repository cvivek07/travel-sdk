package com.ixigo.sdk.util

import androidx.core.graphics.ColorUtils

/** Utility class for color related requirements */
object ColorUtils {

  /**
   * calculate the middle color between two given colors
   * @param startColor
   * @param endColor
   */
  fun getMiddleColor(startColor: Int, endColor: Int) =
      ColorUtils.blendARGB(startColor, endColor, 0.75F)

  /**
   * checks if a given color is light or dark
   * @param color
   */
  fun isLightColor(color: Int): Boolean {
    val colorLuminance = ColorUtils.calculateLuminance(color)
    return colorLuminance > 0.5
  }
}
