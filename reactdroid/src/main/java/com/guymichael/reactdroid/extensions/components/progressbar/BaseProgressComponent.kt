package com.guymichael.reactdroid.extensions.components.progressbar

import android.os.Build
import android.widget.ProgressBar
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent

/** A component which can be both controlled and uncontrolled -
 * Depending on the nullability of its props' `progress` argument,
 * which represents `current progress` and on-user-change `callback` */
abstract class BaseProgressComponent<P : BaseProgressProps, S : OwnState, V : ProgressBar>(
        v: V
    ) : AComponent<P, S, V>(v) {

    private val mProgressHandler by lazy {
        ReactdroidProgressBarWatcher.create(mView) { progress ->
            props.progress?.second?.invoke(progress)
        }
    }

    override fun componentDidMount() {
        if (this.props.progress != null) {
            //we're controlled
            mProgressHandler//invoke lazy which sets (touch) listener on the bar
        }
    }

    override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any?) {
        if (prevProps.progress == null && this.props.progress != null) {
            //we've suddenly turned to be controlled! THINK disallow
            mProgressHandler//invoke lazy (if was't invoked before), which sets (touch) listener on the bar
        }
    }

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
        props.progress?.first?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mView.setProgress(it, props.animateChanges)
            } else {
                mView.progress = it
            }
        }
    }
}