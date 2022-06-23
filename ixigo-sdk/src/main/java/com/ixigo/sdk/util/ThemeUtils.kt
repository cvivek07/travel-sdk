package com.ixigo.sdk.util

import android.graphics.Color
import com.ixigo.sdk.ui.GradientThemeColor
import com.ixigo.sdk.ui.SDKTheme
import com.ixigo.sdk.ui.SolidThemeColor
import com.ixigo.sdk.ui.ThemeColor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.Exception

/** Utility class for validating and parsing string */
object ThemeUtils {

  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val sdkThemeAdapter by lazy { moshi.adapter(SDKTheme::class.java) }

  /**
   * Parse/validate themeString and return a ThemeColor
   * @param themeString
   */
  fun getThemeColor(themeString: String): ThemeColor {
    val sdkTheme = sdkThemeAdapter.fromJson(themeString)
    val solidTheme = sdkTheme?.solidThemeColor
    val gradientTheme = sdkTheme?.gradientThemeColor
    val isValidGradientTheme = validateGradientTheme(gradientTheme)
    return if (isValidGradientTheme) {
      val arrayColor = getGradientThemeColors(gradientTheme)
      GradientThemeColor(Color.parseColor(arrayColor?.get(0)), Color.parseColor(arrayColor?.get(1)))
    } else {
      SolidThemeColor(Color.parseColor(solidTheme))
    }
  }

  /**
   * validates a gradient theme and returns a boolean
   * @param gradientTheme
   */
  fun validateGradientTheme(gradientTheme: String?): Boolean {
    gradientTheme?.let {
      val colorArray = getGradientThemeColors(gradientTheme)
      return colorArray != null
    }
    return false
  }

  /**
   * returns an array of start and end color when input is valid, otherwise returns null
   * @param input
   */
  fun getGradientThemeColors(input: String?): Array<String>? {
    return try {
      arrayOf(
          filterString(input!!).split(",")[0].split(":")[1].trim(),
          filterString(input).split(",")[1].split(":")[1].trim())
    } catch (e: Exception) {
      null
    }
  }

  /**
   * removes all brackets in a given string
   * @param input
   */
  private fun filterString(input: String): String {
    return input.replace("(", "").replace(")", "")
  }
}
