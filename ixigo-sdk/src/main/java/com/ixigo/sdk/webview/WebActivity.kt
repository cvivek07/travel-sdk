package com.ixigo.sdk.webview

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.setFragmentResultListener
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.R
import com.ixigo.sdk.databinding.WebActivityBinding

class WebActivity : AppCompatActivity(), WebViewDelegate {

  @VisibleForTesting internal lateinit var binding: WebActivityBinding
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

    setStatusBarColor(ContextCompat.getColor(this, R.color.ixigosdk_primary_color))
    configureTopExitBar()

    supportFragmentManager.executePendingTransactions()
  }

  private val usingTopExitBar: Boolean by lazy {
    val config = intent.extras?.getParcelable<FunnelConfig>(WebViewFragment.CONFIG)
    config?.enableExitBar ?: IxigoSDK.instance.config.enableExitBar
  }

  private fun configureTopExitBar() {
    if (usingTopExitBar) {
      binding.topExitBarTitle.text = IxigoSDK.instance.appInfo.appName
      setStatusBarColor(ContextCompat.getColor(this, R.color.exit_top_nav_bar_color))
      binding.topExitBar.setOnClickListener {
        supportFragmentManager.setFragmentResultListener(ExitConfirmationResultCode, this) { _, _ ->
          onQuit()
        }
        ExitConfirmationDialogFragment().show(supportFragmentManager, "exit-confirmation")
      }
    } else {
      binding.topExitBar.visibility = View.GONE
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    webViewFragment.onActivityResult(requestCode, resultCode, data)
  }

  private fun setStatusBarColor(color: Int) {
    val luminance = ColorUtils.calculateLuminance(color)
    val isLightColor = luminance > 0.5
    WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = isLightColor
    window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window?.statusBarColor = color
  }

  override fun onQuit() {
    finish()
  }

  override fun updateStatusBarColor(color: Int) {
    if (usingTopExitBar) {
      return
    }
    setStatusBarColor(color)
  }
}
