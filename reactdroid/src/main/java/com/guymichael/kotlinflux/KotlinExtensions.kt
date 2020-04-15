package com.guymichael.kotlinflux

import io.reactivex.rxjava3.schedulers.Timed
import java.util.concurrent.TimeUnit

/**
 * Use to group values by their updated timestamp (date), to have 'lists' in Reducers,
 * as having actual lists is forbidden.
 * **Note: **this method assumes that all [Timed] items are in millis
 */
fun <T> Iterable<Timed<T>>.toTimedReducerMap(): Map<Long, T> { //THINK speed
    return groupBy({ it.time() }) {
        it.value()
    }.mapValues { it.value.last() }
}

/**
 * Use to parse previously set Reducer value using [toTimedReducerMap]
 * @return a sorted [Timed] list
 */
fun <T> Map<Long, T>.fromTimedReducerMap(): List<Timed<T>> { //THINK speed
    return map { Timed(it.value, it.key, TimeUnit.MILLISECONDS) }
        .sortedBy { it.time() }
}