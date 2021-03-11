package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnProps
import java.io.Serializable

/** Simple [CharSequence] props for simple components or component wrappers (HOCs of any kind) */
data class CharSequenceProps(val value: CharSequence?) : OwnProps(), Serializable {
    override fun getAllMembers() = listOf(value)
}

fun Component<CharSequenceProps, *>.onRender(nextValue: CharSequence?) {
    this.onRender(CharSequenceProps(nextValue))
}