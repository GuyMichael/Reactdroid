package com.guymichael.reactdroid.model

import com.guymichael.kotlinreact.model.ComponentDataManager
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.Utils

/**
 * Android [ComponentDataManager]
 */
abstract class AComponentDataManager<P: OwnProps, D : OwnState>
    : ComponentDataManager<P, D>() {

    override fun runOnUiThread(consumer: () -> Unit) {
        Utils.runOnUiThread(0, consumer)
    }
}