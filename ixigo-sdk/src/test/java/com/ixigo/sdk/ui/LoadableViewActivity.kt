package com.ixigo.sdk.ui

import android.app.Activity
import android.os.Bundle
import com.ixigo.sdk.R

class LoadableViewActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.webview_layout)
  }
}
