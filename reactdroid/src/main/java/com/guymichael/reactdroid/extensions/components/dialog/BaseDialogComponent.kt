package com.guymichael.reactdroid.extensions.components.dialog

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.kotlinreact.model.OwnState

/**
 * @param bindToParent A parent [View] to bind the dialog to its lifecycle.
 * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 */
abstract class BaseDialogComponent<P : BaseADialogProps, S : OwnState, D : AlertDialog>(
        val mDialog: Lazy<D>
        , bindToParent: View
        , private val onDismiss: () -> Unit
        , private val onShow: ((P) -> Unit)? = null
        , private val mCustomContent: Lazy<AComponent<P, *, *>>? = null
    ) : AComponent<P, S, View>(bindToParent) {

    /**
     * @param bindToParent A parent [AComponent] to bind the dialog to its lifecycle.
     * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
     *
     * @param onDismiss callback for when the dialog is dismissed but props state is `shown`, e.g. by the user.
     * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
     */
    constructor(dialog: Lazy<D>, bindToParent: AComponent<*, *, *>
            , onDismiss: () -> Unit, onShow: ((P) -> Unit)? = null
            , customContent: Lazy<AComponent<P, *, *>>? = null)
        : this(dialog, bindToParent.mView, onDismiss, onShow, customContent)


    final override fun UNSAFE_componentDidMountHint() {
        super.UNSAFE_componentDidMountHint()

        if (this.props.shown) {
            this.onShow?.invoke(this.props)
        }
    }


    final override fun UNSAFE_componentWillUnmountHint() {
        super.UNSAFE_componentWillUnmountHint()

        if (mDialog.value.isShowing) {
            mDialog.value.dismiss()
        }
    }

    final override fun UNSAFE_componentDidUpdateHint(prevProps: P, prevState: S, snapshot: Any?) {
        super.UNSAFE_componentDidUpdateHint(prevProps, prevState, snapshot)

        if (this.props.shown && !prevProps.shown) {
            this.onShow?.invoke(this.props)
        }
    }

    private fun onBindDialogListeners() {
        //wait for dismiss to align props. Also used when activity is destroyed
        try {
            mDialog.value.setOnDismissListener {
                if (props.shown) {
                    //cancelled/dismissed without props (e.g. outside or back key, or anti-pattern manual dismiss() )
                    onDismiss()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()//THINK handle parent component recreations when dialog is bound to activity,
                               // e.g. when parent is a list item. It may create 2 dialogs
            //resetting throws IllegalStateException (see impl.)
        }
    }

    private fun updateDialogShownState(nextShown: Boolean) {
        if (nextShown != mDialog.value.isShowing) {
            if (nextShown) {
                mDialog.value.show()
            } else {
                mDialog.value.dismiss()
            }
        }
    }

    private fun renderFirstShow() {
        mDialog.value                           //init. dialog
        onBindDialogListeners()                 //bind listeners

        //custom content (?)
        mCustomContent?.value?.also {
            mDialog.value.setView(it.mView)
            it.onRender(this.props)             //render (custom content)
        }
        updateDialogShownState(true)   //show dialog
    }

    private fun render(shown: Boolean) {
        updateDialogShownState(shown)
        mCustomContent?.value?.onRender(this.props)
    }

    final override fun render() {
        when {
            //was already shown
            mDialog.isInitialized() -> render(props.shown)

            //first show (not yet init.)
            props.shown -> renderFirstShow()

            //never showed and still shouldn't
            else -> {}
        }
    }
}