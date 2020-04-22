package com.guymichael.reactdroid.extensions.components.progressbar

import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar

class ReactdroidProgressBarWatcher(private val listener: (Int) -> Unit) : View.OnTouchListener {

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            v.post { //end of execution queue
                listener.invoke((v as ProgressBar).progress)
            }
        }

        return false //never consume
    }

    companion object {
        fun create(view: ProgressBar, listener: (Int) -> Unit): ReactdroidProgressBarWatcher {
            return ReactdroidProgressBarWatcher(listener).also {
                //use touch listener for being notified when user has changed the bar position
                //NOCOMMIT check if it overrides the default behavior and/or enables touches!
                view.setOnTouchListener(it)
            }
        }
    }
}