package com.ixigo.sdk.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ixigo.sdk.DeeplinkHandler
import com.ixigo.sdk.DeeplinkHandlerResult
import com.ixigo.sdk.Handled
import com.ixigo.sdk.NotHandled

class FakeDeeplinkHandler: DeeplinkHandler {
  override fun handleUri(context: Context, uri: Uri): DeeplinkHandlerResult {

    if (uri.toString().contains("deeplink")) {
      val intent = Intent(context, FakeDeeplinkActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
      context.startActivity(intent)
      return Handled
    }
    return NotHandled
  }
}