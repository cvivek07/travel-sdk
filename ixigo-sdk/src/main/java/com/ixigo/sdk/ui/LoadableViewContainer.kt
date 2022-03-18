package com.ixigo.sdk.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.R

class LoadableViewContainer(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

  init {
    inflate(context, R.layout.loadableview_layout, this)
  }

  var onGoBack: (() -> Unit)? = null
  var onRetry: (() -> Unit)? = null

  var status: Status = Loaded
    set(value) {
      getView(field).visibility = GONE
      getView(value).visibility = VISIBLE
      field = value
    }

  private fun getView(status: Status): View =
      when (status) {
        is Loaded -> contentView
        is Failed -> errorView
        is Loading -> loadingView
      }

  @VisibleForTesting
  internal val contentView: View by lazy {
    getChildAt(1).also {
      removeView(it)
      it.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
      it.visibility = VISIBLE
      val container: ViewGroup = findViewById(R.id.container)
      container.addView(it)
    }
  }

  @VisibleForTesting
  internal val loadingView by lazy {
    findViewById<ProgressBar>(R.id.progressView).apply {
      visibility = GONE
      indeterminateTintList = ColorStateList.valueOf(IxigoSDK.instance.theme.primaryColor)
    }
  }

  @VisibleForTesting
  internal val errorView by lazy {
    findViewById<View>(R.id.errorView).apply {
      visibility = GONE
      findViewById<Button>(R.id.retryButton).setOnClickListener { onRetry?.invoke() }
      findViewById<Button>(R.id.backButton).setOnClickListener { onGoBack?.invoke() }
    }
  }
}

sealed class Status

data class Loading(val progress: Float? = null) : Status()

object Loaded : Status()

data class Failed(val errorMessage: String? = null) : Status()
