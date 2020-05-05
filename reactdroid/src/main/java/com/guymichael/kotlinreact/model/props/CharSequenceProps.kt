package com.guymichael.kotlinreact.model.props

import com.guymichael.kotlinreact.model.OwnProps

data class CharSequenceProps(val value: CharSequence) : OwnProps() {
    override fun getAllMembers() = listOf(value)
}