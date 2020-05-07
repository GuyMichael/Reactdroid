package com.guymichael.reactdroid.extensions.components.dialog

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent

/**
 * @param bindToParent A parent [AComponent] to bind the dialog to its lifecycle.
 * Normally the page (Activity), or just any component that when unmounts, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 */
open class CDialog<P : BaseDialogProps>(
        dialog: Lazy<AlertDialog>
        , bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
        , customContent: Lazy<AComponent<P, *, *>>? = null
    ) : BaseAlertDialogComponent<P, EmptyOwnState, AlertDialog>(
        dialog, bindToParent, onDismiss, onShow, customContent
    ) {

    constructor(
        dialog: Lazy<AlertDialog>
        , bindToParent: AComponent<*, *, *>
        , onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
        , customContent: Lazy<AComponent<P, *, *>>? = null
    ): this(dialog, bindToParent.mView, onDismiss, onShow, customContent)

    override fun createInitialState(props: P) = EmptyOwnState
}

class CSimpleDialog(
        dialog: Lazy<AlertDialog>
        , bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((SimpleDialogProps) -> Unit)? = null
    ) : CDialog<SimpleDialogProps>(dialog, bindToParent, onDismiss, onShow) {

    constructor(
        dialog: Lazy<AlertDialog>
        , bindToParent: AComponent<*, *, *>
        , onDismiss: () -> Unit
        , onShow: ((SimpleDialogProps) -> Unit)? = null
    ): this(dialog, bindToParent.mView, onDismiss, onShow)

    fun onRender(shown: Boolean) {
        super.onRender(SimpleDialogProps(shown))
    }
}



//export as a method

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this`, to bind the dialog to this
 * [AComponent]'s lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user. You should then update the `props` (change your state to then call
 * [AComponent.onRender] with `shown = false`) to align with actual dialog state
 */
fun AComponent<*, *, *>.with(dialog: Lazy<AlertDialog>, onDismiss: () -> Unit, onShow: ((SimpleDialogProps) -> Unit)? = null)
    : CSimpleDialog {

    return CSimpleDialog(dialog, Utils.getActivityView(mView) ?: mView, onDismiss, onShow)
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> AComponent<*, *, *>.with(dialog: Lazy<AlertDialog>
        , customContent: Lazy<AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return CDialog(dialog, Utils.getActivityView(mView) ?: mView, onDismiss, onShow, customContent)
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param customLayout a [Pair] of layout resource to Component[AComponent] supplier
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> AComponent<*, *, *>.with(dialog: Lazy<AlertDialog>
        , customLayout: Pair<Int, (View) -> AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return with(dialog, lazy {
        customLayout.second.invoke(
            LayoutInflater.from(mView.context!!).inflate(customLayout.first, null)//THINK null context
        )
    }, onDismiss, onShow)
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this`, to bind the dialog to this
 * [AComponent]'s lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user. You should then update the `props` (change your state to then call
 * [AComponent.onRender] with `shown = false`) to align with actual dialog state
 */
fun ComponentActivity<*>.with(dialog: Lazy<AlertDialog>, onDismiss: () -> Unit, onShow: ((SimpleDialogProps) -> Unit)? = null)
    : CSimpleDialog {

    return CSimpleDialog(dialog, Utils.getActivityView(this)!!, onDismiss, onShow)
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> ComponentActivity<*>.with(dialog: Lazy<AlertDialog>
        , customContent: Lazy<AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return CDialog(dialog, Utils.getActivityView(this)!!, onDismiss, onShow, customContent)//THINK null view
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param customLayout a [Pair] of layout resource to Component[AComponent] supplier
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> ComponentActivity<*>.with(dialog: Lazy<AlertDialog>
        , customLayout: Pair<Int, (View) -> AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return with(dialog, lazy {
        customLayout.second.invoke(
            LayoutInflater.from(this).inflate(customLayout.first, null)
        )
    }, onDismiss, onShow)
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this`, to bind the dialog to this
 * [AComponent]'s lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user. You should then update the `props` (change your state to then call
 * [AComponent.onRender] with `shown = false`) to align with actual dialog state
 */
fun ComponentFragment<*>.with(dialog: Lazy<AlertDialog>, onDismiss: () -> Unit, onShow: ((SimpleDialogProps) -> Unit)? = null)
    : CSimpleDialog {

    return CSimpleDialog(dialog, Utils.getActivityView(this.view!!)!!, onDismiss, onShow)//THINK null view
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> ComponentFragment<*>.with(dialog: Lazy<AlertDialog>
        , customContent: Lazy<AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return CDialog(dialog, Utils.getActivityView(this.view!!)!!, onDismiss, onShow, customContent)//THINK null view
}

/**
 * creates a simple [dialog][CDialog] with `bindToParent = this.mView`, to bind the dialog to this
 * [AComponent]'s Activity lifecycle.
 *
 * @param customLayout a [Pair] of layout resource to Component[AComponent] supplier
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`,
 * e.g. by the user.
 * You should then update your local state ([AComponent.setState]) to reflect `shown = false`)
 */
fun <P : BaseDialogProps> ComponentFragment<*>.with(dialog: Lazy<AlertDialog>
        , customLayout: Pair<Int, (View) -> AComponent<P, *, *>>
        , /** update your local state to reflect the shown state */
          onDismiss: () -> Unit
        , onShow: ((P) -> Unit)? = null
    ) : CDialog<P> {

    return with(dialog, lazy {
        customLayout.second.invoke(
            LayoutInflater.from(this.view!!.context).inflate(customLayout.first, null)//THINK null view/context
        )
    }, onDismiss, onShow)
}