package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.EmptyOwnProps

fun Component<EmptyOwnProps, *>.onRender() {
    onRender(EmptyOwnProps)
}

inline fun <R> Boolean?.letIfTrue(crossinline mapper: () -> R?): R? {
    return this.takeIf { it == true }?.let { mapper() }
}