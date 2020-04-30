package com.guymichael.reactdroid.extensions.components.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.StyleRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.extensions.components.progressbar.CProgress
import com.guymichael.reactdroid.extensions.components.progressbar.SimpleProgressProps
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.core.model.AHOC
import java.lang.ref.WeakReference

/**
 * @param bindToParent A parent [AComponent] to bind the dialog to its lifecycle.
 * Normally the page (Activity), or just any component that when unmounts, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 */
class CProgressDialog(
        bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((ProgressDialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<ProgressDialogProps, *, *>>?
            = defaultCircularProgressComponent(bindToParent.context)
        , @StyleRes themeResId: Int? = null
    ) : BaseProgressDialogComponent<ProgressDialogProps, EmptyOwnState>(
            bindToParent, onDismiss, onShow, customContent, themeResId
    ) {

    constructor(
        bindToParent: AComponent<*, *, *>
        , onDismiss: () -> Unit
        , onShow: ((ProgressDialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<ProgressDialogProps, *, *>>?
            = defaultCircularProgressComponent(bindToParent.mView.context)
        , @StyleRes themeResId: Int? = null
    ): this(bindToParent.mView, onDismiss, onShow, customContent, themeResId)

    override fun createInitialState(props: ProgressDialogProps) = EmptyOwnState


    companion object {
        @JvmStatic
        fun defaultCircularProgressComponent(context: Context)
                : Lazy<AComponent<ProgressDialogProps, *, *>> {
            val contextRef = WeakReference(context)

            return lazy { //assume context is alive when called
                AHOC.from<ProgressDialogProps, SimpleProgressProps, ProgressBar>(
                    CProgress(ProgressBar(contextRef.get()!!))
                ) { it.progressProps }
            }
        }
    }
}


//export as a method

/**
 * creates a progress [dialog][CProgressDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun AComponent<*, *, *>.withProgress(
        onDismiss: () -> Unit
        , onShow: ((ProgressDialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<ProgressDialogProps, *, *>>
            = CProgressDialog.defaultCircularProgressComponent(mView.context)
        , @StyleRes themeResId: Int? = null
    ) : CProgressDialog {

    return CProgressDialog(
        Utils.getActivityView(mView) ?: mView
        , onDismiss, onShow, customContent, themeResId
    )
}

/**
 * creates a progress [dialog][CProgressDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param customLayout a [Pair] of layout resource to Component[AComponent] supplier
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun AComponent<*, *, *>.withProgress(
        customLayout: Pair<Int, (View) -> AComponent<ProgressDialogProps, *, *>>
        , onDismiss: () -> Unit
        , onShow: ((ProgressDialogProps) -> Unit)? = null
        , @StyleRes themeResId: Int? = null
    ) : CProgressDialog {

    return withProgress(onDismiss, onShow, lazy {
        customLayout.second.invoke(
            LayoutInflater.from(mView.context!!).inflate(customLayout.first, null)//THINK null context
        )
    }, themeResId)
}