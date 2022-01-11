package com.ixigo.sdk.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import com.ixigo.sdk.R

class LoadableViewContainer(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

  var onGoBack: (() -> Unit)? = null

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
    getChildAt(0).apply {
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
      visibility = VISIBLE
    }
  }

  @VisibleForTesting
  internal val loadingView: ProgressBar by lazy {
    ProgressBar(context, null, android.R.attr.progressBarStyleLarge).also {
      val layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
      layoutParams.addRule(CENTER_IN_PARENT, TRUE)
      it.layoutParams = layoutParams
      it.visibility = GONE
      it.indeterminateTintList =
          ColorStateList.valueOf(resources.getColor(R.color.ixigosdk_primary_color))
      addView(it)
    }
  }

  @VisibleForTesting
  internal val errorView: View by lazy {
    Button(context).also { button ->
      val layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
      layoutParams.addRule(CENTER_IN_PARENT, TRUE)
      button.layoutParams = layoutParams
      button.visibility = GONE
      button.text = "Go Back"
      button.setOnClickListener { onGoBack?.invoke() }
      addView(button)
    }
  }
}

sealed class Status

data class Loading(val progress: Float? = null) : Status()

object Loaded : Status()

data class Failed(val errorMessage: String? = null) : Status()
