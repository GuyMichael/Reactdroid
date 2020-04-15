package com.guymichael.kotlinflux.model

import com.guymichael.kotlinreact.model.ComponentDataManager
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState

abstract class ConnectedComponentDataManager<API_PROPS: OwnProps, DATA_PROPS : OwnState>
    : ComponentDataManager<API_PROPS, DATA_PROPS>() {

    final override fun runOnUiThread(consumer: () -> Unit) {
        runOnUiThread(150, consumer)

        //TODO we don't like timeouts.
        //TODO The reason is that we want to run this AFTER the
        //TODO 'last' dispatch (which was called inside loadAndCacheData() )
        //THINK consider adding ability to store, to run something on the next state change (after the differedBuffer)
    }

    abstract fun runOnUiThread(delay: Long = 0, consumer: () -> Unit)
}