package com.guymichael.reactdroid.extensions.router

import android.content.Intent
import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.core.activity.ComponentActivity

inline fun <K, V, R : Any> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
    return mapValuesNotNullTo(LinkedHashMap(), transform)
}

inline fun <K, V, R : Any> Map<K, V>.mapValuesNotNullTo(destination: MutableMap<K, R>, transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
    forEach { element -> transform(element)?.let { destination.put(element.key, it) } }
    return destination
}


/**
 * @return a promise for when the task is done. If `intent` contains no deep link data,
 * a **rejected** promise will be returned
 */
fun ComponentActivity<*>.openDeepLinkOrReject(intent: Intent): APromise<Unit> {
    return intent.data?.let {
        DeepLinkLogic.openDeepLink(this, it.toString())

    } ?: APromise.ofReject("intent contains no valid deep link data")
}