package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnState
import java.io.Serializable

/** Simple [Double] state for simple components or component wrappers (HOCs of any kind) */
data class DoubleState(val value: Double?) : OwnState(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<*, DoubleState>.setState(nextValue: Double?) {
    this.setState(DoubleState(nextValue))
}