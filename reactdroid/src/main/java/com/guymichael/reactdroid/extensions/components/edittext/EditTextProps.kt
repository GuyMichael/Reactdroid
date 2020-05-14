package com.guymichael.reactdroid.extensions.components.edittext

import com.guymichael.kotlinreact.model.OwnProps


data class EditTextProps<I : Any>(val value: I?) : OwnProps() {
    override fun getAllMembers() = listOf(value)
}