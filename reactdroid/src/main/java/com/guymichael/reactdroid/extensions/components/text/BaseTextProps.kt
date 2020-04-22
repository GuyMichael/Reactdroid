package com.guymichael.reactdroid.extensions.components.text

import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseTextProps(open val text: CharSequence?) : OwnProps() {
    override fun getAllMembers(): List<*> = listOf(
        text
    )
}