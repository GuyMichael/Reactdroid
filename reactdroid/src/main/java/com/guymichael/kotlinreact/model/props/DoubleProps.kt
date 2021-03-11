package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [Double] props for simple components or component wrappers (HOCs of any kind) */
data class DoubleProps(val value: Double?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<DoubleProps, *>.onRender(nextValue: Double?) {
    this.onRender(DoubleProps(nextValue))
}