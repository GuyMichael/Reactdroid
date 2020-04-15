package com.guymichael.reactdroid

import com.guymichael.reactdroid.model.Component
import com.guymichael.reactdroid.model.EmptyOwnProps

fun Component<EmptyOwnProps, *>.onRender() {
    onRender(EmptyOwnProps)
}

inline fun <R> Boolean?.letIfTrue(crossinline mapper: () -> R?): R? {
    return this.takeIf { it == true }?.let { mapper() }
}