package com.guymichael.reactdroid

import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.model.AComponent
import io.reactivex.rxjava3.disposables.Disposable

fun AComponent<*, *, *>.renderFullWidth(fullWidth: Boolean) {
    mView.layoutParams?.let {
        when {
            fullWidth && it.width != ViewGroup.LayoutParams.MATCH_PARENT -> {
                it.width = ViewGroup.LayoutParams.MATCH_PARENT
            }

            !fullWidth && it.width != ViewGroup.LayoutParams.WRAP_CONTENT -> {
                it.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }
}

fun AComponent<*, *, *>.renderMarginsPx(top: Int? = null, start: Int? = null
                                        , bottom: Int? = null, end: Int? = null) {

    mView.renderMarginsPx(top = top, start = start, bottom = bottom, end = end)
}

fun AComponent<*, *, *>.renderMarginsRes(@DimenRes top: Int? = null, @DimenRes start: Int? = null
                                         , @DimenRes bottom: Int? = null, @DimenRes end: Int? = null) {

    mView.renderMarginsRes(top = top, start = start, bottom = bottom, end = end)
}

fun APromise<*>.executeAutoHandleErrorMessage(component: AComponent<*, *, *>, autoCancel: Boolean = false): Disposable {
    return executeAutoHandleErrorMessage(component.mView, autoCancel)
}

fun AComponent<*, *, *>.getText(@StringRes resId: Int): CharSequence {
    return mView.context?.getText(resId) ?: ""
}

fun AComponent<*, *, *>.getString(@StringRes textRes: Int, vararg format: Any): String {
    return mView.context?.getString(textRes, *format) ?: ""
}