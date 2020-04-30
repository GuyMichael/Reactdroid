package com.guymichael.reactdroid.extensions.components.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.guymichael.reactdroid.core.getText
import com.guymichael.reactdroid.core.model.AComponent

object ModalDialog {

    @JvmStatic
    fun of(context: Context, title: CharSequence? = null, message: CharSequence? = null
        , positiveBtn: Pair<CharSequence, ((DialogInterface) -> Unit)?>? = null
        , negativeBtn: Pair<CharSequence, ((DialogInterface) -> Unit)?>? = null
        , neutralBtn: Pair<CharSequence, ((DialogInterface) -> Unit)?>? = null
        , @StyleRes style: Int = 0): AlertDialog.Builder {

        return AlertDialog.Builder(context, style)
            .setTitle(title)
            .setMessage(message)
            .apply {
                positiveBtn?.also {
                    setPositiveButton(it.first) { d, _ -> it.second?.invoke(d) }
                }

                negativeBtn?.also {
                    setNegativeButton(it.first) { d, _ -> it.second?.invoke(d) }
                }

                neutralBtn?.also {
                    setNeutralButton(it.first) { d, _ -> it.second?.invoke(d) }
                }
            }
    }

    fun of(context: Context, @StringRes title: Int? = null, @StringRes  message: Int? = null
        , positiveBtn: Pair<Int, ((DialogInterface) -> Unit)?>? = null
        , negativeBtn: Pair<Int, ((DialogInterface) -> Unit)?>? = null
        , neutralBtn: Pair<Int, ((DialogInterface) -> Unit)?>? = null
        , @StyleRes style: Int = 0): AlertDialog.Builder {

        return of(
            context
            , title?.let { context.getText(it) }
            , message?.let { context.getText(it) }
            , positiveBtn?.let { it.first.let { res -> context.getText(res) } to it.second }
            , negativeBtn?.let { it.first.let { res -> context.getText(res) } to it.second }
            , neutralBtn?.let { it.first.let { res -> context.getText(res) } to it.second }
            , style
        )
    }
}




//as global static methods

fun AComponent<*, *, *>.dialog(title: CharSequence? = null, message: CharSequence? = null
        , positiveBtn: Pair<CharSequence, (() -> Unit)?>? = null
        , negativeBtn: Pair<CharSequence, (() -> Unit)?>? = getText(android.R.string.cancel) to null
        , neutralBtn: Pair<CharSequence, (() -> Unit)?>? = null
        , @StyleRes style: Int = 0
    ): AlertDialog {

    return ModalDialog.of(
        mView.context!! //THINK null context
        , title
        , message
        , positiveBtn?.let { it.first to it.second?.let { { _: DialogInterface -> it.invoke() } } }
        , negativeBtn?.let { it.first to it.second?.let { { _: DialogInterface -> it.invoke() } } }
        , neutralBtn?.let { it.first to it.second?.let { { _: DialogInterface -> it.invoke() } } }
        , style
    ).create()
}

fun AComponent<*, *, *>.dialog(@StringRes title: Int? = null, @StringRes message: Int? = null
        , positiveBtn: Pair<Int, (() -> Unit)?>? = null
        , negativeBtn: Pair<Int, (() -> Unit)?>? = android.R.string.cancel to null
        , neutralBtn: Pair<Int, (() -> Unit)?>? = null
        , @StyleRes style: Int = 0
    ): AlertDialog {

    return dialog(
        title?.takeIf { it != 0 }?.let(::getText)
        , message?.takeIf { it != 0 }?.let(::getText)
        , positiveBtn?.takeIf { it.first != 0 }?.let { getText(it.first) to it.second }
        , negativeBtn?.takeIf { it.first != 0 }?.let { getText(it.first) to it.second }
        , neutralBtn?.takeIf { it.first != 0 }?.let { getText(it.first) to it.second }
        , style
    )
}