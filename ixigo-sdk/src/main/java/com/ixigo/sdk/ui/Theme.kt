package com.ixigo.sdk.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.ixigo.sdk.R

/**
 * Theme is used to customize the UI of of certain chrome elements presented by the SDK eg: status
 * bar color, loader colors, spacing, etc...
 *
 * @property primaryColor
 */
data class Theme(val primaryColor: Int)

/**
 * This theme will read values from xml resources configuration
 *
 * @param context
 * @return
 */
fun defaultTheme(context: Context) =
    Theme(primaryColor = ContextCompat.getColor(context, R.color.ixigosdk_primary_color))
