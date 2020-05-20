package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.ownstate.*
import com.guymichael.kotlinreact.model.props.*
import com.guymichael.reactdroid.core.model.AComponent


inline fun <R> Boolean?.letIfTrue(crossinline mapper: () -> R?): R? {
    return this.takeIf { it == true }?.let { mapper() }
}


fun Component<EmptyOwnProps, *>.onRender() {
    onRender(EmptyOwnProps)
}


fun AComponent<*, BooleanState, *>.setState(ownState: Boolean) = setState(BooleanState(ownState))
fun AComponent<*, IntState, *>.setState(ownState: Int) = setState(IntState(ownState))
fun AComponent<*, LongState, *>.setState(ownState: Long) = setState(LongState(ownState))
fun AComponent<*, StringState, *>.setState(ownState: String) = setState(StringState(ownState))
fun AComponent<*, DoubleState, *>.setState(ownState: Double) = setState(DoubleState(ownState))
    //disabled to not be confused with setState(String) which is also a CharSequence:
//fun AComponent<*, CharSequenceState, *>.setState(value: CharSequence) = setState(CharSequenceState(value))

fun AComponent<BooleanProps, *, *>.onRender(props: Boolean) = onRender(BooleanProps(props))
fun AComponent<IntProps, *, *>.onRender(props: Int) = onRender(IntProps(props))
fun AComponent<LongProps, *, *>.onRender(props: Long) = onRender(LongProps(props))
fun AComponent<StringProps, *, *>.onRender(props: String) = onRender(StringProps(props))
fun AComponent<DoubleProps, *, *>.onRender(props: Double) = onRender(DoubleProps(props))
