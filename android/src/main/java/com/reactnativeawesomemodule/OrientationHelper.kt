package com.reactnativeawesomemodule

import android.content.Context
import android.view.OrientationEventListener

class OrientationHelper(context: Context) : OrientationEventListener(context) {

  private var lastOrientation = -1

  fun getOrientation(): Int {
    return lastOrientation
  }

  override fun onOrientationChanged(orientation: Int) {
    if (orientation == ORIENTATION_UNKNOWN) return

    lastOrientation = when (orientation) {
      in 45..134 -> 90
      in 135..224 -> 180
      in 225..314 -> 270
      else -> 0
    }
  }

}
