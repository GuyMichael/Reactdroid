package com.guymichael.reactdroid.extensions.components.dialog

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.model.AComponent

open class CAlertDialog<CUSTOM_CONTENT_PROPS : OwnProps>(
        bindToParent: View
        , onDismiss: () -> Unit
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , customContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>? = null
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , @StyleRes style: Int = 0
    ) : BaseAlertDialogComponent<CUSTOM_CONTENT_PROPS, AlertDialogProps<CUSTOM_CONTENT_PROPS>, EmptyOwnState, AlertDialog>(
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
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , customContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>? = null
        , dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , @StyleRes style: Int = 0
    ): this(bindToParent.mView, onDismiss, onShow, customContent, dialogBuilder, style)


    internal var mDialog: AlertDialog? = null //THINK protected in super?



    private fun renderAlertDialog(dialog: AlertDialog) {
        renderText(getTitleView(dialog), props.title)
        renderText(getMessageView(dialog), props.message)
        renderBtn(getOkBtn(dialog), props.positiveBtn?.first, props.positiveBtn?.second)
        renderBtn(getCancelBtn(dialog), props.negativeBtn?.first, props.negativeBtn?.second)
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

    override fun createInitialState(props: AlertDialogProps<CUSTOM_CONTENT_PROPS>) = EmptyOwnState
}









/* export as methods */

/** Creates and binds a dialog with custom content, to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent lazy component to set as the dialog's custom view
 */
fun <CUSTOM_CONTENT_PROPS : OwnProps> AComponent<*, *, *>.withAlertDialog(
        dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , customContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>
        , onDismiss: () -> Unit
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , @StyleRes style: Int = 0
    ): CAlertDialog<CUSTOM_CONTENT_PROPS> {

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
fun <CUSTOM_CONTENT_PROPS : OwnProps> AComponent<*, *, *>.withAlertDialog(
        dialogBuilder: (AlertDialog.Builder) -> AlertDialog
        , customContent: Pair<Int, (View) -> AComponent<CUSTOM_CONTENT_PROPS, *, *>>
        , onDismiss: () -> Unit
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , @StyleRes style: Int = 0
    ): CAlertDialog<CUSTOM_CONTENT_PROPS> {

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
 */
fun AComponent<*, *, *>.withAlertDialog(
    dialogBuilder: (AlertDialog.Builder) -> AlertDialog
    , onDismiss: () -> Unit
    , onShow: ((AlertDialogProps<OwnProps>) -> Unit)? = null
    , @StyleRes style: Int = 0
): CAlertDialog<OwnProps> {

    return CAlertDialog(
        Utils.getActivityView(mView) ?: mView
        , onDismiss, onShow, null, dialogBuilder, style
    )
}

/** Creates and binds a dialog to the activity of this component.
 *
 * @param onDismiss use to update the dialog 'shown' state (e.g. [AComponent.setState])
 * @param customContent lazy component to set as the dialog's custom view
 * @param cancelable sets the following:
 * [AlertDialog.Builder.setCancelable], [AlertDialog.setCanceledOnTouchOutside]
 */
fun <CUSTOM_CONTENT_PROPS : OwnProps> AComponent<*, *, *>.withAlertDialog(
        onDismiss: () -> Unit
        , customContent: Lazy<AComponent<CUSTOM_CONTENT_PROPS, *, *>>
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , cancelable: Boolean = false
        , @StyleRes style: Int = 0
    ): CAlertDialog<CUSTOM_CONTENT_PROPS> {

    return withAlertDialog(
        dialogBuilder = { b ->
            b.setCancelable(cancelable)
            .create().also { d ->
                d.setCanceledOnTouchOutside(cancelable)
            }
        }
        , onDismiss = onDismiss
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
fun <CUSTOM_CONTENT_PROPS : OwnProps> AComponent<*, *, *>.withAlertDialog(
        onDismiss: () -> Unit
        , customContent: Pair<Int, (View) -> AComponent<CUSTOM_CONTENT_PROPS, *, *>>
        , onShow: ((AlertDialogProps<CUSTOM_CONTENT_PROPS>) -> Unit)? = null
        , cancelable: Boolean = false
        , @StyleRes style: Int = 0
    ): CAlertDialog<CUSTOM_CONTENT_PROPS> {

    return withAlertDialog(
        onDismiss
        , customContent = lazy { customContent.second.invoke(
            LayoutInflater.from(mView.context).inflate(customContent.first, null)
        )}
        , onShow = onShow
        , cancelable = cancelable
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
        , onShow: ((AlertDialogProps<OwnProps>) -> Unit)? = null
        , cancelable: Boolean = false
        , @StyleRes style: Int = 0
    ): CAlertDialog<OwnProps> {

    return CAlertDialog(
        Utils.getActivityView(mView) ?: mView
        , onDismiss
        , onShow
        , null
        , dialogBuilder = { b ->
            b.setCancelable(cancelable)
                .create().also { d ->
                    d.setCanceledOnTouchOutside(cancelable)
                }
        }
        , style = style
    )
}