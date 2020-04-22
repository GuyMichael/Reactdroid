package com.guymichael.reactdroid.extensions.components.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StyleRes
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.model.AComponent
import java.lang.ref.WeakReference

/**
 * @param bindToParent A parent [View] to bind the dialog to its lifecycle.
 * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 */
abstract class BaseProgressDialogComponent<P : BaseProgressDialogProps<*>, S : OwnState>(
        bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
        , customContent: Lazy<AComponent<P, *, *>>? = null
        , @StyleRes themeResId: Int?
    ) : BaseDialogComponent<P, S, Dialog>
        (createLazyDialog(bindToParent.context, themeResId), bindToParent, onDismiss, onShow, customContent) {

    /**
     * @param bindToParent A parent [AComponent] to bind the dialog to its lifecycle.
     * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
     *
     * @param onDismiss callback for when the dialog is dismissed but props state is `shown`, e.g. by the user.
     * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
     */
    constructor(bindToParent: AComponent<*, *, *>
            , onDismiss: () -> Unit, onShow: ((P) -> Unit)? = null
            , customContent: Lazy<AComponent<P, *, *>>? = null
            , @StyleRes themeResId: Int?
    ): this(bindToParent.mView, onDismiss, onShow, customContent, themeResId)


    override fun renderDialogContent(dialog: Dialog, beforeFirstShow: Boolean) {

        /* set only before first show */
        if (beforeFirstShow) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setCanceledOnTouchOutside(props.canceledOnTouchOutside)
            dialog.setCancelable(props.cancelable)

            props.dimBackground.also { dim ->
                //dimmed by default
                if( !dim) {
                    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
            }
        }
    }
}





private fun createLazyDialog(context: Context, @StyleRes themeResId: Int? = null): Lazy<Dialog> {
    val contextRef = WeakReference(context)

    return lazy { //assume context is alive when called
        if (themeResId == null) Dialog(context) else Dialog(contextRef.get()!!, themeResId)
    }
}