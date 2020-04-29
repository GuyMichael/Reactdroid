package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [Boolean] props for simple components or component wrappers (HOCs of any kind) */
data class BooleanProps(val value: Boolean) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}