package com.guymichael.reactdroid.extensions.components.dialog

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.R
import com.guymichael.reactdroid.core.viewVisibilityOf
import com.guymichael.reactdroid.extensions.animation.renderVisibility

/** Must be called from inside a `render` method */
internal fun getTitleView(dialog: AlertDialog): TextView? {

    return try {
            dialog.findViewById(R.id.alertTitle)
        } catch (e: Throwable) {
            Logger.w(CAlertDialog::class, "getTitleView() failed with fixed titleId(R.id.alertTitle): ${e.message}")
            null //replace with commented code below if fails all the time (something to do with v7 dialog vs. normal)

            /*getAppContext().resources.getIdentifier("android:id/alertTitle", null, null)
                .takeIf { it != 0 }?.let { titleId ->
                    try {
                        dialog.findViewById<TextView>(titleId)
                    } catch (e: Throwable) {
                        Logger.w(ChPassDialog::class, "getTitleView() failed with resolved titleId($titleId): ${e.message}")
                        null
                    }
                }*/
        }
}

/** Must be called from inside a `render method` */
internal fun getMessageView(dialog: AlertDialog): TextView? {
    return try {
        dialog.findViewById(android.R.id.message)
    } catch (e: Throwable) {
        Logger.w(CAlertDialog::class, "getMessageView() failed: ${e.message}")
        null
    }
}

internal fun getOkBtn(dialog: AlertDialog): Button? {
    return try {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    } catch (e: Throwable) {
        Logger.w(CAlertDialog::class, "getOkBtn() failed: ${e.message}")
        null
    }
}

internal fun getCancelBtn(dialog: AlertDialog): Button? {
    return try {
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
    } catch (e: Throwable) {
        Logger.w(CAlertDialog::class, "getOkBtn() failed: ${e.message}")
        null
    }
}

internal fun getNeutralBtn(dialog: AlertDialog): Button? {
    return try {
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
    } catch (e: Throwable) {
        Logger.w(CAlertDialog::class, "getOkBtn() failed: ${e.message}")
        null
    }
}

internal fun renderText(
        view: TextView?
        , value: CharSequence?
    ) {

    view?.takeIf { it.text?.toString() != value?.toString() }?.also {
        it.text = value
        it.renderVisibility(viewVisibilityOf(!value.isNullOrBlank()), animate = false)
    }
}

internal fun CAlertDialog.renderBtn(
        btn: Button?
        , value: CharSequence?
        , onClick: ((props: DialogProps) -> APromise<*>?)? = null
    ) {

    btn?.takeIf { it.text?.toString() != value?.toString() }?.also {
        it.text = value
        it.renderVisibility(viewVisibilityOf(!value.isNullOrBlank()), animate = false)

        it.setOnClickListener {
            onClick?.invoke(this.props)
            ?.then {
                mDialog?.dismiss()
            }
            ?.execute()
            ?: mDialog?.dismiss()
        }
    }
}