package com.rasalexman.easyrecyclerbinding.common

import android.os.Handler
import android.os.Looper
import android.view.View

abstract class ViewClickersListener(
    private val hasDoubleClickListener: Boolean,
    private val doubleClickDelayTime: Long,
    private val consumeLongClick: Boolean = true
) : View.OnClickListener, View.OnLongClickListener {

    private var lastClickCount: Int = 0

    override fun onClick(v: View) {
        if (hasDoubleClickListener) {
            processDoubleClick()
        } else {
            onSingleClicked()
        }
    }

    private fun processDoubleClick() {
        if (lastClickCount == 0) {
            lastClickCount++
            Handler(Looper.getMainLooper()).postDelayed({
                if (lastClickCount == 1) {
                    onSingleClicked()
                } else {
                    onDoubleClicked()
                }
                lastClickCount = 0
            }, doubleClickDelayTime)
        } else {
            lastClickCount++
        }
    }

    // true if the callback consumed the long click, false otherwise.
    override fun onLongClick(view: View): Boolean {
        onLongClicked()
        return consumeLongClick
    }

    abstract fun onLongClicked()

    abstract fun onSingleClicked()

    abstract fun onDoubleClicked()
}