package com.guymichael.reactdroid.model

import com.guymichael.reactdroid.TimeoutUtils

abstract class IntervalRunnable : Runnable {
    private var intervalMs: Long = 0

    val isTicking: Boolean
        get() = TimeoutUtils.isRunnablePending(this)

    override fun run() {
        if (onTick()) {
            synchronized(TimeoutUtils.mTimeoutHandler) {
                TimeoutUtils.mTimeoutHandler.postDelayed(this, intervalMs)
            }
        } else {
            TimeoutUtils.clearTimeout(this)
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