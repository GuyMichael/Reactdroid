package com.guymichael.reactdroid.core

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.core.model.AComponent
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

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

/**
 * Executes the promise on a `component`, giving you control over:
 * * Canceling automatically when `component`'s `Context` (`Activity`) is destroyed
 * * Handling (e.g. `Toast`) errors automatically.
 *
 * Note: if both `autoCancel` and `handleErrorMessage` are `false`, there is no use
 * of this method. Use [execute] instead
 *
 * @param component
 * @param autoCancel see [APromise.executeWhileAlive]. Default is `false`, as you might fetch
 * some data (which can be cached) and you don't want to lose it
 * @param handleErrorMessage see [APromise.setGlobalAutoErrorHandler]. Default is `true`
 */
fun APromise<*>.execute(component: AComponent<*, *, *>
        , autoCancel: Boolean = false
        , handleErrorMessage: Boolean = true)
    : Disposable {

    return execute(component.mView, autoCancel, handleErrorMessage)
}

/**
 * Uses the Activity [View]'s [View.post] (or [View.postDelayed]) and converts it to a promise.
 * This is somewhat same as using [APromise.executeWhileAlive] and [APromise.ofDelay]
 * but uses Android/View infrastructure instead:
 * Easily resolve the promise **at end of execution queue** (delay = 0), while breaking
 * if the view is destroyed.
 *
 * NOTICE: does not yet support cancelling the promise if the view gets destroyed before
 *  the action has been executed. Also meaning that [APromise.finally] will not work when cancelled
 */
fun <A : Activity> APromise.Companion.post(context: A, delayMs: Long = 0L): APromise<A> {
    val activityRef = WeakReference(context)
    val activityName = context.javaClass.simpleName

    return ofCallback { promiseCallback ->
        val postRunnable = {
            activityRef.get()
                ?.let(promiseCallback::onSuccess)
                ?: promiseCallback.onCancel("${activityName}'s Activity already destroyed")
        }

        activityRef.get()?.let {
            Utils.getActivityView(it)
                ?.let { v ->
                if (delayMs > 0) {
                    v.postDelayed(postRunnable, delayMs)
                } else {
                    v.post(postRunnable)
                }
            }
        }

        //if activityRef is empty or its view not found
        ?: promiseCallback.onCancel("${activityName}'s Activity View already destroyed")
    }
}

fun AComponent<*, *, *>.getText(@StringRes resId: Int): CharSequence {
    return mView.context?.getText(resId) ?: ""
}

fun AComponent<*, *, *>.getString(@StringRes textRes: Int, vararg format: Any): String {
    return mView.context?.getString(textRes, *format) ?: ""
}