package com.guymichael.reactdroid.extensions.components.progressbar

import android.os.Build
import android.widget.ProgressBar
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent

/** A controlled component which can be both controlled and uncontrolled -
 * Depending on the nullability of its props' `progress` argument */
abstract class BaseProgressComponent<P : BaseProgressProps, S : OwnState, V : ProgressBar>(
        v: V
    ) : AComponent<P, S, V>(v) {

    override fun render() {
        //update min/max
        // note: both methods below check for actual value change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            props.min?.let {
                mView.min = it
            }

            props.max?.let {
                mView.max = it
            }
        }

        //update progress.
        // note: both methods below check for actual value change
        props.progress?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mView.setProgress(it, props.animateChanges)
            } else {
                mView.progress = it
            }
        }
    }
}