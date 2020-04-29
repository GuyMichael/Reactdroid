package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [String] props for simple components or component wrappers (HOCs of any kind) */
data class StringProps(val value: String?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}