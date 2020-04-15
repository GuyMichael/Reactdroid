package com.guymichael.kotlinflux.model.actions

import com.guymichael.kotlinflux.model.StoreKey

//can't be 'open' as it is first checked by the Reducer code (as its getReducer() throws) THINK
class GenericAction<T>(key : String, value : T?) : Action(object : StoreKey {
    override fun getName(): String {
        return key
    }

    @Throws(IllegalAccessException::class)
    override fun getReducer(): Nothing {
        throw IllegalAccessException("generic actions are not associated with any reducer")
    }
}, value)