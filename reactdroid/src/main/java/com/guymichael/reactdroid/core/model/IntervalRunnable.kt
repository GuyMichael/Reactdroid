package com.guymichael.reactdroid.core.model

import com.guymichael.reactdroid.core.IntervalUtils

abstract class IntervalRunnable : Runnable {
    private var intervalMs: Long = 0

    val isTicking: Boolean
        get() = IntervalUtils.isRunnablePending(this)

    override fun run() {
        if (onTick()) {
            synchronized(IntervalUtils.mTimeoutHandler) {
                IntervalUtils.mTimeoutHandler.postDelayed(this, intervalMs)
            }
        } else {
            IntervalUtils.clearInterval(this)
        }
    }

    fun setInterval(intervalMs: Long): IntervalRunnable {
        this.intervalMs = intervalMs
        return this
    }

    /**
     * @return True to keep on ticking. False to stop the interval runs.
     */
    abstract fun onTick(): Boolean
}