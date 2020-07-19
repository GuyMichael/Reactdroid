package com.guymichael.kotlinflux.extensions.data

import com.guymichael.apromise.APromise
import com.guymichael.kotlinflux.extensions.data.model.StoreDataAPIController
import com.guymichael.kotlinflux.extensions.data.model.StoreDataType
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

/** See [StoreDataAPIController.withDataDispatch] for docs */
inline fun <reified API_RESPONSE : Any> APromise<API_RESPONSE>.withDataDispatch(
    //store/data connection
    dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> Unit>>

    //optionals
    , noinline persistSideEffects: (API_RESPONSE) -> Unit = {}
    , noinline dispatchSideEffects: (API_RESPONSE) -> Unit = {}
): APromise<API_RESPONSE> {

    return StoreDataAPIController.withDataDispatch(
        this
        , dataTypes
        , persistSideEffects
        , dispatchSideEffects
    )
}

/** See [StoreDataAPIController.prepare] for docs */
inline fun <reified API_RESPONSE : Any, DATA : Any, TYPE: StoreDataType<DATA>>
        APromise<API_RESPONSE>.withDataDispatch(

    //store/data connection
    dataType: TYPE
    , noinline mapResponseToData: (API_RESPONSE) -> List<DATA>
    , merge: Boolean

    //optionals
    , noinline persistSideEffects: (API_RESPONSE) -> Unit = {}
    , noinline dispatchSideEffects: (API_RESPONSE) -> Unit = {}
    , persist: Boolean = true
): APromise<API_RESPONSE> {

    return StoreDataAPIController.withDataDispatch(
        this
        , listOf(
            dataType to { res -> dataType.dispatchLoaded(mapResponseToData(res), merge, persist) }
        )
        , persistSideEffects
        , dispatchSideEffects
    )
}