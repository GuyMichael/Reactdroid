package com.guymichael.reactdroid.extensions.components.input

import com.guymichael.kotlinreact.model.OwnProps


data class InputProps<I : Any>(val value: I?) : OwnProps() {
    override fun getAllMembers() = listOf(value)
}