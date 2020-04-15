package com.guymichael.kotlinflux.extensions.data.model

internal fun MutableMap<String, Any>.putAll(dataList: List<Any>?, idSupplier: (d: Any) -> String?) {
    dataList?.forEach {
        idSupplier(it)?.let { id ->
            this[id] = it
        }
    }
}

internal fun MutableMap<String, Any>.putAllNotNull(other: Map<String, Any?>?) {
    other?.keys?.forEach { id ->
        other[id]?.let {
            this[id] = it
        }
    }
}