package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.apromise.APromise

data class DialogProps(
    override val shown: Boolean
    , val title: CharSequence?
    , val message: CharSequence?
    , val okBtn: Pair<CharSequence, ((props: DialogProps) -> APromise<*>?)?>?
    , val cancelBtn: Pair<CharSequence, ((props: DialogProps) -> APromise<*>?)?>? = null
    , val neutralBtn: Pair<CharSequence, ((props: DialogProps) -> APromise<*>?)?>? = null
//    , val cancelable: Boolean = false AlertDialog internal method doesn't check for newCancelable != prev
) : BaseDialogProps(shown) {

    override fun getAllMembers() = listOf(
        shown, title, message, /*cancelable,*/ okBtn?.first, cancelBtn?.first, neutralBtn?.first
    )
}