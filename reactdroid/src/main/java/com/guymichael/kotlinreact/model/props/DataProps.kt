package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** props for components or component wrappers (HOCs of any kind) that hold a single data model.
 * When using kotlin, this model should be a "data class" with only val members (immutable) */
data class DataProps<T : Any>(val data: T) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(data)
}