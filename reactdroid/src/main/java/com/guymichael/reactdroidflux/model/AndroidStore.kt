package com.guymichael.reactdroidflux.model

import android.os.Build
import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.Store
import com.guymichael.kotlinflux.model.reducers.Reducer
import com.guymichael.reactdroid.core.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * should be inherited with 'object' (Kotlin's singleton) declaration
 */
abstract class AndroidStore(reducer : Reducer, preloadedState: GlobalState?)
    : Store(reducer, preloadedState, AndroidSchedulers.mainThread(), getComputationScheduler()) {

    override fun isOnUiThread() = Utils.isOnUiThread()
}

private fun getComputationScheduler() : Scheduler {
    return Build.VERSION.SDK_INT.let { version -> when {
        version >= Build.VERSION_CODES.O//8.0
             -> Schedulers.computation()//THINK best scheduler. Computation results in delayed dispathes on some occasions, main thread is slow

        version >= Build.VERSION_CODES.N//7.0
             -> Schedulers.computation()//THINK best scheduler. Computation results in delayed dispathes on some occasions, main thread is slow

        else -> AndroidSchedulers.mainThread()
    }}
}