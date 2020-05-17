package com.guymichael.reactdroid.extensions.components.dialog

import com.guymichael.reactdroid.extensions.components.progressbar.BaseProgressProps
import com.guymichael.reactdroid.extensions.components.progressbar.SimpleProgressProps

abstract class BaseProgressDialogProps<P : BaseProgressProps>(
    shown: Boolean
    , open val dimBackground: Boolean
    , open val cancelable: Boolean
    , open val canceledOnTouchOutside: Boolean
    , open val progressProps: P
) : BaseDialogProps(shown) {

    override fun getAllMembers() = listOf(
        shown, dimBackground, cancelable, canceledOnTouchOutside, progressProps
    )
}

data class ProgressDialogProps(
    override val shown: Boolean
    , override val dimBackground: Boolean = false
    , override val cancelable: Boolean = false
    , override val canceledOnTouchOutside: Boolean = cancelable
    , override val progressProps: SimpleProgressProps
): BaseProgressDialogProps<SimpleProgressProps>
    (shown, dimBackground, cancelable, canceledOnTouchOutside, progressProps) {

    companion object {
        @JvmStatic
        fun open() = ProgressDialogProps(
            true, progressProps = SimpleProgressProps(null)
        )

        @JvmStatic
        fun hidden() = ProgressDialogProps(
            false, progressProps = SimpleProgressProps(null)
        )
    }
}