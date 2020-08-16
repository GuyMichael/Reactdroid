package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps

data class AlertDialogProps<CUSTOM_CONTENT_PROPS : OwnProps>(
    override val shown: Boolean
    , val title: CharSequence?
    , val message: CharSequence?
    , val positiveBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>?
    , val negativeBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>? = null
    , val neutralBtn: Pair<CharSequence, ((props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> APromise<*>?)?>? = null
//    , val cancelable: Boolean = false AlertDialog internal method doesn't check for newCancelable != prev
    , override val customContentProps: CUSTOM_CONTENT_PROPS? = null
) : BaseAlertDialogProps<CUSTOM_CONTENT_PROPS>(shown, customContentProps) {

    override fun getAllMembers() = listOf(
        shown, title, message, /*cancelable,*/ positiveBtn?.first, negativeBtn?.first, neutralBtn?.first
        , customContentProps
    )

    companion object {
        @JvmStatic
        fun <CUSTOM_CONTENT_PROPS : OwnProps> from(
            shown: Boolean
            , title: CharSequence?
            , msg: CharSequence?
            , positiveBtn: CharSequence?
            , negativeBtn: CharSequence? = null
            , neutralBtn: CharSequence? = null
            , customContentProps: CUSTOM_CONTENT_PROPS? = null
        ): AlertDialogProps<CUSTOM_CONTENT_PROPS> {

            return AlertDialogProps(
                shown, title, msg
                , positiveBtn?.let { it to null }
                , negativeBtn?.let { it to null }
                , neutralBtn?.let { it to null }
                , customContentProps
            )
        }

        @JvmStatic
        fun from(
            shown: Boolean
            , title: CharSequence?
            , msg: CharSequence?
            , positiveBtn: CharSequence?
            , negativeBtn: CharSequence? = null
            , neutralBtn: CharSequence? = null
        ): AlertDialogProps<OwnProps> {

            return AlertDialogProps(
                shown, title, msg
                , positiveBtn?.let { it to null }
                , negativeBtn?.let { it to null }
                , neutralBtn?.let { it to null }
                , null
            )
        }
    }
}