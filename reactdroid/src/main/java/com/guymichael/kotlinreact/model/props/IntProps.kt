package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [Int] props for simple components or component wrappers (HOCs of any kind) */
data class IntProps(val value: Int?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}