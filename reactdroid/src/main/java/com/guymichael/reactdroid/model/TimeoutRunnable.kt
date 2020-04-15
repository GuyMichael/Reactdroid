package com.guymichael.reactdroid.model

import com.guymichael.reactdroid.TimeoutUtils

/**
 * When implementing [.run], please call super to retain ability - in case you use this Runnable
 * with a key, by calling [Utilities.setTimeout]
 */
abstract class TimeoutRunnable : Runnable {

    /**
     * If extending, please call super to retain ability.
     */
    override fun run() {
        execute()

        TimeoutUtils.clearTimeout(this)
    }

    abstract fun execute()
}