package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnState
import java.io.Serializable

/** Simple [String] state for simple components or component wrappers (HOCs of any kind) */
data class StringState(val value: String?) : OwnState(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<*, StringState>.setState(nextValue: String?) {
    this.setState(StringState(nextValue))
}