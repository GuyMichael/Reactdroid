package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.OwnState
import java.io.Serializable

/** Simple [Boolean] state for simple components or component wrappers (HOCs of any kind) */
data class BooleanState(val value: Boolean) : OwnState(), Serializable {
    override fun getAllMembers() = listOf(value)
}