package com.ixigo.sdk.util

import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringUtilsTest {

  @Test
  fun `validate gradient method returns true when valid input is given`() {
    val input = "(start-color: #721053, end-color: #AD2E41)"
    val isValid = ThemeUtils.validateGradientTheme(input)
    assertTrue(isValid)
  }

  @Test
  fun `validate gradient method returns false when invalid input is given`() {
    val input = ""
    val isValid = ThemeUtils.validateGradientTheme(input)
    assertFalse(isValid)
  }

  @Test
  fun `validate gradient method returns false when null input is given`() {
    val input = null
    val isValid = ThemeUtils.validateGradientTheme(input)
    assertFalse(isValid)
  }

  @Test
  fun `given valid input returns array of start and end color`() {
    val input = "(start-color: #721053, end-color: #AD2E41)"
    val outputArray = ThemeUtils.getGradientThemeColors(input)
    assertEquals("#721053", outputArray?.get(0) ?: "")
    assertEquals("#AD2E41", outputArray?.get(1) ?: "")
  }

  @Test
  fun `given invalid input returns null`() {
    val input = ""
    val gradientThemeColors = ThemeUtils.getGradientThemeColors(input)
    assertNull(gradientThemeColors)
  }
}
