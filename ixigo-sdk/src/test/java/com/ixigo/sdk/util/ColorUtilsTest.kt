package com.ixigo.sdk.util

import android.graphics.Color
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ColorUtilsTest {

  private val colorUtils = mock<ColorUtils>()

  @Test
  fun `return false if luminance of given color is more than 50 percent`() {
    val givenColor = "#721053"
    val isLight = colorUtils.isLightColor(Color.parseColor(givenColor))
    assertFalse(isLight)
  }
}
