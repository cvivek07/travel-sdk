package com.ixigo.sdk.webview

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import com.ixigo.sdk.R
import com.ixigo.sdk.databinding.WebActivityBinding

class WebActivity : AppCompatActivity(), WebViewDelegate {

  private lateinit var binding: WebActivityBinding
  private lateinit var webViewFragment: WebViewFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    webViewFragment = WebViewFragment()
    webViewFragment.delegate = this
    webViewFragment.arguments = intent.extras

    binding = WebActivityBinding.inflate(layoutInflater)

    supportFragmentManager
        .beginTransaction()
        .add(binding.fragmentContainerView.id, webViewFragment)
        .commit()
    setContentView(binding.root)

    setStatusBarColor()

    supportFragmentManager.executePendingTransactions()
  }

  private fun setStatusBarColor() {
    val luminance =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          ColorUtils.calculateLuminance(resources.getColor(R.color.ixigosdk_primary_color, theme))
        } else {
          @Suppress("DEPRECATION")
          ColorUtils.calculateLuminance(resources.getColor(R.color.ixigosdk_primary_color))
        }
    val isLightColor = luminance > 0.5
    WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = isLightColor
  }

  override fun onQuit() {
    finish()
  }
}
