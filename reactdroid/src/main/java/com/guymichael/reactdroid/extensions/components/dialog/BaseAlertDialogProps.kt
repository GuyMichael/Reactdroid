package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseAlertDialogProps<CUSTOM_CONTENT_PROPS : OwnProps>(
    override val shown: Boolean
//    , val cancelable: Boolean = false AlertDialog internal method doesn't check for newCancelable != prev
    , open val customContentProps: CUSTOM_CONTENT_PROPS? = null
) : BaseDialogProps(shown) {

    override fun getAllMembers(): List<*>  = listOf(
        shown, /*cancelable,*/ customContentProps
    )
}