package com.ixigo.sdk.webview

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.R
import com.ixigo.sdk.databinding.WebActivityBinding
import com.ixigo.sdk.payment.PaymentSDK
import com.ixigo.sdk.ui.GradientThemeColor
import com.ixigo.sdk.ui.SolidThemeColor
import com.ixigo.sdk.ui.ThemeColor

class WebActivity : AppCompatActivity(), WebViewDelegate, UrlLoader {

  @VisibleForTesting internal lateinit var binding: WebActivityBinding
  private lateinit var webViewFragment: WebViewFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    webViewFragment = WebViewFragment()
    webViewFragment.paymentSDK = PaymentSDK.instance
    webViewFragment.delegate = this
    webViewFragment.arguments = intent.extras

    binding = WebActivityBinding.inflate(layoutInflater)

    supportFragmentManager
        .beginTransaction()
        .add(binding.fragmentContainerView.id, webViewFragment)
        .commit()
    setContentView(binding.root)

    setStatusBarColor(SolidThemeColor(IxigoSDK.instance.theme.primaryColor))
    configureTopExitBar()

    supportFragmentManager.executePendingTransactions()

    onBackPressedDispatcher.addCallback(webViewFragment.webViewBackPressHandler)
  }

  private val usingTopExitBar: Boolean by lazy {
    val config = intent.extras?.getParcelable<FunnelConfig>(WebViewFragment.CONFIG)
    config?.enableExitBar ?: IxigoSDK.instance.config.enableExitBar
  }

  private fun configureTopExitBar() {
    if (usingTopExitBar) {
      binding.topExitBarTitle.text = IxigoSDK.instance.appInfo.appName
      setStatusBarColor(
          SolidThemeColor(ContextCompat.getColor(this, R.color.exit_top_nav_bar_color)))
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

  private fun setStatusBarColor(themeColor: ThemeColor) {
    when (themeColor) {
      is GradientThemeColor -> {
        val gradient =
            GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(themeColor.startColor, themeColor.endColor))
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars =
            themeColor.isLight
        with(window) {
          addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
          statusBarColor = resources.getColor(android.R.color.transparent)
          setBackgroundDrawable(gradient)
        }
      }
      is SolidThemeColor -> {
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars =
            themeColor.isLight
        with(window) {
          addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
          statusBarColor = themeColor.color
        }
      }
    }
  }

  override fun onQuit() {
    finish()
  }

  override fun updateStatusBarColor(color: ThemeColor) {
    if (usingTopExitBar) {
      return
    }
    setStatusBarColor(color)
  }

  override fun loadUrl(url: String, headers: Map<String, String>?) {
    runOnUiThread { webViewFragment.loadUrl(url, headers) }
  }
}
