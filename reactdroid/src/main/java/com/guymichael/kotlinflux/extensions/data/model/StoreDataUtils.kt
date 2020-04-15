package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState

class StoreDataUtils { companion object {
    fun isDataLoading(vararg dataTypes: StoreDataType<*>, state: GlobalState) : Boolean {
        return dataTypes.map { it.isDataLoading(state) }.contains(true)
    }
}}