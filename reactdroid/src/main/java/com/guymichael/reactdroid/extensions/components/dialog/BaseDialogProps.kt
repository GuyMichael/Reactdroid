package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.kotlinreact.model.OwnProps


abstract class BaseDialogProps(open val shown: Boolean) : OwnProps() {
    override fun getAllMembers(): List<*> = listOf(
        shown
    )
}

data class SimpleDialogProps(override val shown: Boolean) : BaseDialogProps(shown)