package com.ixigo.sdk.app

import android.app.Activity
import android.app.AlertDialog

class ProgressDialog(private val activity: Activity) {

  private val dialog: AlertDialog by lazy {
    AlertDialog.Builder(activity).setCancelable(false).setView(R.layout.loader).create()
  }

  fun show() {
    dialog.show()
  }

  fun hide() {
    activity.runOnUiThread { dialog.hide() }
  }
}
