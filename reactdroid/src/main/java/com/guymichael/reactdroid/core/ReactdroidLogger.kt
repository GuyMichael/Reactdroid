package com.guymichael.reactdroid.core

import android.util.Log
import com.guymichael.kotlinreact.Logger
import com.guymichael.promise.LoggerIntf

class ReactdroidLogger: LoggerIntf {
    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun shouldLogI() =
        iEnabled
    override fun shouldLogD() =
        dEnabled
    override fun shouldLogW() =
        wEnabled
    override fun shouldLogE() =
        eEnabled

    companion object {
        private var iEnabled: Boolean = false
        private var dEnabled: Boolean = false
        private var wEnabled: Boolean = false
        private var eEnabled: Boolean = false

        fun enableLogging(i: Boolean = true, d: Boolean = true, w: Boolean = true, e: Boolean = true) {
            iEnabled = i
            dEnabled = d
            wEnabled = w
            eEnabled = e

            Logger.init(ReactdroidLogger())
        }
    }
}