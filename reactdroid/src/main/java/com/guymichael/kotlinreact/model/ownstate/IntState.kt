package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [Int] state for simple components or component wrappers (HOCs of any kind) */
data class IntState(val value: Int?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<*, IntState>.setState(nextValue: Int?) {
    this.setState(IntState(nextValue))
}