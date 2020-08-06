package com.guymichael.reactdroid.core

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import com.guymichael.apromise.APromise
import com.guymichael.apromise.ViewUtils
import com.guymichael.promise.Logger
import com.guymichael.reactdroid.core.model.AComponent
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

/** See [APromise.autoCancel] with view */
fun <T> APromise<T>.withAutoCancel(component: AComponent<*, *, *>): APromise<T> {
    return autoCancel(component.mView)
}

/** See [APromise.Companion.setGlobalAutoErrorHandler]  */
fun <T> APromise<T>.withGlobalErrorHandling(component: AComponent<*, *, *>): APromise<T> {
    return Utils.getActivity(component.mView)?.let {
        autoHandleErrorMessage(it)
    } ?: APromise.ofReject("null activity for component ${component.javaClass.simpleName}")
}


fun <C : AComponent<*, *, *>> WeakReference<C>.getIfAlive(requireMounted: Boolean): C? {
    return this.get()?.takeIf { ViewUtils.isAlive(it.mView, requireMounted) }
}

/** skips (only) this consumer if the component or its context became null, or component isn't mounted */
fun <T, C : AComponent<*, *, *>> APromise<T>.thenWithComponent(
        component: C
        , requireMounted: Boolean = true
        , consumer: (C, T) -> Unit
    ): APromise<T> {

    val componentRef = WeakReference(component)

    return then {
        componentRef.getIfAlive(requireMounted)?.let { v ->
            consumer.invoke(v, it)
        }
        //or skip
        ?: Logger.d(this.javaClass, "thenWithComponent - skipping consumer - null component")
    }
}

/**
 * Uses the Activity [View]'s [View.post] (or [View.postDelayed]) and converts it to a promise.
 * This is somewhat same as using [APromise.ofDelay] but uses Android/View infrastructure instead
 *
 * NOTICE: does not yet support cancelling the promise if the view gets destroyed before
 *  the action has been executed. Also meaning that [APromise.finally] will not work when cancelled
 */
fun <A : Activity> APromise.Companion.ofPost(context: A, delayMs: Long = 0L): APromise<A> {
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