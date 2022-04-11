package com.ixigo.sdk

import android.content.Context
import android.net.Uri

interface DeeplinkHandler {
  fun handleUri(context: Context, uri: Uri): DeeplinkHandlerResult
}

sealed class DeeplinkHandlerResult

object NotHandled : DeeplinkHandlerResult()

object Handled : DeeplinkHandlerResult()
