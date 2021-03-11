package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnState
import java.io.Serializable

/** Simple [Long] state for simple components or component wrappers (HOCs of any kind) */
data class LongState(val value: Long?) : OwnState(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<*, LongState>.setState(nextValue: Long?) {
    this.setState(LongState(nextValue))
}