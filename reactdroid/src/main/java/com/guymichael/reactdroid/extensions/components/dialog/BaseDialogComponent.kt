package com.guymichael.reactdroid.extensions.components.dialog

import android.app.Dialog
import android.view.View
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.kotlinreact.model.OwnState

/**
 * @param bindToParent A parent [View] to bind the dialog to its lifecycle.
 * Normally the page/Activity view, or just any `View` that when gone, this dialog should begone altogether
 *
 * @param onDismiss callback for when the dialog is dismissed but `props` state is `shown`, e.g. by the user.
 * You should then update the `props` (call [onRender] with `shown = false`) to align with actual dialog state
 */
abstract class BaseDialogComponent<P : BaseDialogProps, S : OwnState, D : Dialog>(
        private val mDialog: Lazy<D>
        , bindToParent: View
        , private val onDismiss: () -> Unit
        , private val onShow: ((P) -> Unit)? = null
        , /** calls [Dialog.setContentView] which sets the screen content to an explicit view */
          private val mCustomContent: Lazy<AComponent<P, *, *>>?
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
        , customContent: Lazy<AComponent<P, *, *>>? = null
    ): this(dialog, bindToParent.mView, onDismiss, onShow, customContent)


    /* API / callbacks */

    /** This is the `render` method of this component.
     * With the showing/hiding of the dialog already handled for you, this is where you can
     * adjust the contents or other properties of the dialog
     *
     * @param beforeFirstShow if true, the dialog has never been shown before and is about to be,
     * after this method/callback finishes
     * */
    protected abstract fun renderDialogContent(dialog: D, beforeFirstShow: Boolean)




    /* Lifecycle */

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




    /* Privates */

    private fun onBindDialogListeners(dialog: D) {
        //wait for dismiss to align props. Also used when activity is destroyed
        try {
            dialog.setOnDismissListener {
                if (this.props.shown) {
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



    private fun renderOnFirstShow() {
        mDialog.value.also { d ->                       //initializes the dialog (lazy)
            onBindDialogListeners(d)                    //bind listeners

            renderDialogContent(d, true)   //callback for extending classes
            mCustomContent?.also {
                d.setContentView(it.value.mView)        //init the component and set its view
                it.value.onRender(this.props)           //first content render
            }
            updateDialogShownState(d, true)    //show dialog (first time)
        }
    }

    private fun renderStandard(shown: Boolean) {
        mDialog.value.also { d ->
            updateDialogShownState(d, shown)
            renderDialogContent(d, false)
        }

        if (shown) {
            mCustomContent?.value?.onRender(this.props)
        }
    }

    final override fun render() {
        when {
            //was already shown
            mDialog.isInitialized() -> renderStandard(props.shown)

            //first show (not yet init.)
            props.shown -> renderOnFirstShow()

            //never showed and still shouldn't
            else -> {}
        }
    }
}










private fun updateDialogShownState(dialog: Dialog, nextShown: Boolean) {
    if (nextShown != dialog.isShowing) {
        if (nextShown) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }
}