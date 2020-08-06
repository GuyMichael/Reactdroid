package com.guymichael.reactdroid.extensions.components.dialog

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.renderOrGone

/**
 * @param bindToParent A parent [View] to bind the dialog to its lifecycle.
 * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 *
 * @param mCustomInnerContent content inside AlertDialog, below the 'message' and above the buttons section.
 */
abstract class BaseAlertDialogComponent
<CUSTOM_CONTENT_PROPS : OwnProps, P : BaseAlertDialogProps<CUSTOM_CONTENT_PROPS>, S : OwnState, D : AlertDialog>
    (
        dialog: Lazy<D>
        , bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
        , private val mCustomInnerContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>? = null
            //different from super class's 'mCustomContent', as it sets the content inside the AlertDialog,
            //instead of replacing the whole dialog view (as super's 'mCustomContent' does)
    ) : BaseDialogComponent<P, S, D>(dialog, bindToParent, onDismiss, onShow, null) {

    /**
     * @param bindToParent A parent [AComponent] to bind the dialog to its lifecycle.
     * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
     *
     * @param onDismiss callback for when the dialog is dismissed but props state is `shown`, e.g. by the user.
     * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
     */
    constructor(dialog: Lazy<D>
        , bindToParent: AComponent<*, *, *>
        , onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
        , customContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>? = null
    ): this(dialog, bindToParent.mView, onDismiss, onShow, customContent)


    override fun renderDialogContent(dialog: D, beforeFirstShow: Boolean) {
        //render custom content if exists
        mCustomInnerContent?.value?.also { customContentComponent ->

            if (beforeFirstShow) {
                //set the dialog custom view, before showing
                dialog.setView(customContentComponent.mView)
            }

            customContentComponent.renderOrGone(props.customContentProps)
        }
    }
}