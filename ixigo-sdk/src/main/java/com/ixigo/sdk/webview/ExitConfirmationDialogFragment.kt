package com.ixigo.sdk.webview

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.R

const val ExitConfirmationResultCode: String = "ExitResultCode"

class ExitConfirmationDialogFragment : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let {
      val builder = AlertDialog.Builder(it, R.style.AlertDialogTheme)
      builder
          .setMessage(
              getString(
                  R.string.ixigosdk_exit_top_bar_confirmation, IxigoSDK.instance.appInfo.appName))
          .setPositiveButton(R.string.ixigosdk_go_back) { _, _ ->
            setFragmentResult(ExitConfirmationResultCode, Bundle())
          }
          .setNegativeButton(R.string.ixigosdk_cancel, null)
      builder.create()
    }
        ?: throw IllegalStateException("Activity cannot be null")
  }
}
