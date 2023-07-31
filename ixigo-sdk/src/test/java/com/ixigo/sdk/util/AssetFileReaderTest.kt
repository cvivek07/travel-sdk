package com.ixigo.sdk.util

import android.content.Context
import android.content.res.AssetManager
import java.io.ByteArrayInputStream
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class AssetFileReaderTest {

  @Mock internal lateinit var mockContext: Context
  @Mock internal lateinit var mockAssetManager: AssetManager

  private lateinit var assetFileReader: AssetFileReader

  @Before
  fun setup() {
    assetFileReader = AssetFileReader(mockContext)
  }

  @Test
  fun `test readFile`() {
    val content = "sdk-content"
    Mockito.`when`(mockAssetManager.open(any()))
        .thenReturn(ByteArrayInputStream(content.toByteArray()))
    Mockito.`when`(mockContext.assets).thenReturn(mockAssetManager)

    val fileName = "sdk"
    val actualContent = assetFileReader.readFile(fileName)

    Mockito.verify(mockContext).assets
    Mockito.verify(mockAssetManager).open(eq(fileName))
    assertEquals(content, actualContent)
  }
}
