package com.guymichael.kotlinreact.model.ownstate

import com.guymichael.kotlinreact.model.OwnState
import java.io.Serializable

/** Simple [CharSequence] state for simple components or component wrappers (HOCs of any kind) */
data class CharSequenceState(val value: CharSequence?) : OwnState(), Serializable {
    override fun getAllMembers() = listOf(value)
}