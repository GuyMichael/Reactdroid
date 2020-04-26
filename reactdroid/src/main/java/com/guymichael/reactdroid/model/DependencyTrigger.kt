package com.guymichael.reactdroid.model

import android.os.Handler
import android.os.Looper
import java.util.*

open class DependencyTrigger(vararg dependencyKeys: String
        , private val callback: (fulfilledState: Boolean, DependencyTrigger) -> Unit
    ) {

    private val mDependencyKeys = setOf(*dependencyKeys)
    private val mValues = HashMap<String, Any?>()
    private val mNotifyHandler: Handler = Handler(Looper.getMainLooper())
    private val mDependencyCheckRunnable = DependencyCheckRunnable()
    private var wasFulfilled: Boolean = false
    private var canNotify = true//default


    fun put(key: String, value: Any): DependencyTrigger {
        //don't let same value trigger dependency check
        if (this.mValues[key] == value) {
            return this
        }

        //add property
        this.mValues[key] = value

        //check if dependencies are met (on end of execution chain, in case more 'put' calls are chained)
        mNotifyHandler.removeCallbacks(mDependencyCheckRunnable)
        mNotifyHandler.post(mDependencyCheckRunnable)

        return this
    }

    /**
     * Set whether to notify on fulfillment state changes. Default is [canNotify]
     * @param notify
     */
    fun setNotifyEnabled(notify: Boolean): DependencyTrigger {
        this.canNotify = notify
        return this
    }

    /**
     * Force rechecking, normally if event was set to not notify on fulfilment.
     * If dependecies were met, this will notify the callback
     * @see .setNotifyOnFulfilment
     */
    fun checkAndNotify() {
        this.mDependencyCheckRunnable.checkDependencies(true)
    }

    /** Triggers this event 'now', regardless of missing keys */
    fun forceTrigger() {
        callback(wasFulfilled, this)
    }

    private inner class DependencyCheckRunnable : Runnable {
        override fun run() {
            checkDependencies(canNotify)
        }

        /** @return new fulfilled state */
        fun checkDependencies(notify: Boolean): Boolean {
            for (key in mDependencyKeys) {
                if( !mValues.containsKey(key)) {//allows nulls
                    //not fulfilled
                    if (wasFulfilled && notify) {
                        //state changed
                        callback(false, this@DependencyTrigger)
                    }
                    return false
                }
            }

            //fulfilled
            if( !wasFulfilled && notify) {
                //state changed
                callback(true, this@DependencyTrigger)
            }

            return true
        }
    }
}