package com.guymichael.reactdroid.extensions.components.dialog

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.model.AComponent

open class CAlertDialog(
        bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((DialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<DialogProps, *, *>>? = null
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , @StyleRes style: Int = 0
    ) : BaseAlertDialogComponent<DialogProps, EmptyOwnState, AlertDialog>(
        lazy {
            ModalDialog.of(
                bindToParent.context!! //THINK null context
                , "."
                , "."
                , "." to { _: DialogInterface -> }
                , "." to { _: DialogInterface -> }
                , "." to { _: DialogInterface -> }
                , style
            ).let { dialogBuilder.invoke(it) }
        }
        , bindToParent
        , onDismiss
        , onShow
        , customContent
    ) {

    constructor(
        bindToParent: AComponent<*, *, *>
        , onDismiss: () -> Unit
        , onShow: ((DialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<DialogProps, *, *>>? = null
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , @StyleRes style: Int = 0
    ): this(bindToParent.mView, onDismiss, onShow, customContent, dialogBuilder, style)


    internal var mDialog: AlertDialog? = null //THINK protected in super?



    private fun renderAlertDialog(dialog: AlertDialog) {
        renderText(getTitleView(dialog), props.title)
        renderText(getMessageView(dialog), props.message)
        renderBtn(getOkBtn(dialog), props.okBtn?.first, props.okBtn?.second)
        renderBtn(getCancelBtn(dialog), props.cancelBtn?.first, props.cancelBtn?.second)
        renderBtn(getNeutralBtn(dialog), props.neutralBtn?.first, props.neutralBtn?.second)
    }

    override fun renderDialogContent(dialog: AlertDialog, beforeFirstShow: Boolean) {
        //render shown state and AComponent content
        super.renderDialogContent(dialog, beforeFirstShow)

        //render AlertDialog attributes.
        if (beforeFirstShow) {
            dialog.setOnShowListener { (it as? AlertDialog?)?.also { d ->
                this.mDialog = d
                renderAlertDialog(d)
            }}

        } else if (props.shown) {
            renderAlertDialog(dialog)
        }
    }

    override fun createInitialState(props: DialogProps) = EmptyOwnState
}









/* export as methods */

/** Creates and binds a dialog to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent lazy component to set as the dialog's custom view
 */
fun AComponent<*, *, *>.withDialog(
        onDismiss: () -> Unit
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , onShow: ((DialogProps) -> Unit)? = null
        , customContent: Lazy<AComponent<DialogProps, *, *>>? = null
        , @StyleRes style: Int = 0
    ): CAlertDialog {

    return CAlertDialog(
        Utils.getActivityView(mView) ?: mView
        , onDismiss, onShow, customContent, dialogBuilder, style
    )
}

/** Creates and binds a dialog to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent layoutRes to component creator, for the dialog's custom view
 */
fun AComponent<*, *, *>.withDialog(
        onDismiss: () -> Unit
        , customContent: Pair<Int, (View) -> AComponent<DialogProps, *, *>>
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , onShow: ((DialogProps) -> Unit)? = null
        , @StyleRes style: Int = 0
    ): CAlertDialog {

    return CAlertDialog(
        Utils.getActivityView(mView) ?: mView
        , onDismiss, onShow
        , customContent = lazy { customContent.second.invoke(
            LayoutInflater.from(mView.context).inflate(customContent.first, null)
        )}
        , dialogBuilder = dialogBuilder
        , style = style
    )
}

/** Creates and binds a dialog to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent lazy component to set as the dialog's custom view
 * @param cancelable sets the following:
 * [AlertDialog.Builder.setCancelable], [AlertDialog.setCanceledOnTouchOutside]
 */
fun AComponent<*, *, *>.withAlertDialog(
        onDismiss: () -> Unit
        , onShow: ((DialogProps) -> Unit)? = null
        , cancelable: Boolean = false
        , customContent: Lazy<AComponent<DialogProps, *, *>>? = null
        , @StyleRes style: Int = 0
    ): CAlertDialog {

    return withDialog(
        onDismiss
        , dialogBuilder = { b ->
            b.setCancelable(cancelable)
            .create().also { d ->
                d.setCanceledOnTouchOutside(cancelable)
            }
        }
        , onShow = onShow
        , customContent = customContent
        , style = style
    )
}

/** Creates and binds a dialog to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent layoutRes to component creator, for the dialog's custom view
 * @param cancelable sets the following:
 * [AlertDialog.Builder.setCancelable], [AlertDialog.setCanceledOnTouchOutside]
 */
fun AComponent<*, *, *>.withAlertDialog(
        onDismiss: () -> Unit
        , customContent: Pair<Int, (View) -> AComponent<DialogProps, *, *>>
        , onShow: ((DialogProps) -> Unit)? = null
        , cancelable: Boolean = false
        , @StyleRes style: Int = 0
    ): CAlertDialog {

    return withAlertDialog(
        onDismiss
        , onShow
        , customContent = lazy { customContent.second.invoke(
            LayoutInflater.from(mView.context).inflate(customContent.first, null)
        )}
        , cancelable = cancelable
        , style = style
    )
}