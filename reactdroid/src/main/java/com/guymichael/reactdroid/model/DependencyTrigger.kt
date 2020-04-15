package com.guymichael.reactdroid.model

import android.os.Handler
import android.os.Looper
import java.util.*
//THINK replace dependencyKeys with any 'data class' :)
open class DependencyTrigger(vararg dependencyKeys: String, private val callback: (DependencyTrigger) -> Unit) {
    private val mDependencyKeys: Set<String>
    private val mValues: MutableMap<String, Any>
    private val mNotifyHandler: Handler = Handler(Looper.getMainLooper())
    private val mDependencyCheckRunnable: DependencyCheckRunnable

    private var canNotifyOnFulfilment = true//default

    init {
        //some threads do not have a looper and it will crash. Maybe there is a better solution than going to the main thread..
        this.mDependencyCheckRunnable = DependencyCheckRunnable()
        this.mValues = HashMap()

        this.mDependencyKeys = setOf(*dependencyKeys)
    }

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
     * Default is [.canNotifyOnFulfilment]
     * @param notifyOnFulfillment
     */
    fun setNotifyOnFulfilment(notifyOnFulfillment: Boolean): DependencyTrigger {
        this.canNotifyOnFulfilment = notifyOnFulfillment
        return this
    }

    /**
     * Force rechecking, normally if event was set to not notify on fulfilment.
     * If dependecies were met, this will notify the callback
     * @see .setNotifyOnFulfilment
     */
    fun checkAndNotify() {
        this.mDependencyCheckRunnable.checkDependenciesFulfilled(true)
    }

    /**
     * Triggers this event 'now', regardless of missing keys
     */
    fun forceTrigger() {
        callback(this)
    }

    private inner class DependencyCheckRunnable : Runnable {
        override fun run() {
            checkDependenciesFulfilled(canNotifyOnFulfilment)
        }

        fun checkDependenciesFulfilled(notifyIfFulfilled: Boolean): Boolean {
            for (key in mDependencyKeys) {
                if (!mValues.containsKey(key)) {//allows nulls
                    return false
                }
            }

            if (notifyIfFulfilled) {
                callback(this@DependencyTrigger)
            }

            return true
        }
    }
}