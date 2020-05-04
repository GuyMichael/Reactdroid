package com.guymichael.kotlinflux.extensions.data

import java.util.*

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

/** Returns an immutable copy of this. */
fun <K, V> Map<K, V>.toImmutableMap(): Map<K, V> {
    return if (isEmpty()) {
        emptyMap()
    } else {
        Collections.unmodifiableMap(LinkedHashMap(this))
    }
}