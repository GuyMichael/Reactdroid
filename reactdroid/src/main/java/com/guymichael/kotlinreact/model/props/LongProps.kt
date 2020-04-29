package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [Long] props for simple components or component wrappers (HOCs of any kind) */
data class LongProps(val value: Long?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}