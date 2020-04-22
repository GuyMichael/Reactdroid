package com.guymichael.reactdroid.extensions.components.progressbar

import android.os.Build
import android.widget.ProgressBar
import com.guymichael.reactdroid.model.AComponent

//NOCOMMIT incomplete untested
abstract class BaseProgressComponent<P : BaseProgressProps, S : BaseProgressOwnState<S>, V : ProgressBar>(
    v: V) : AComponent<P, S, V>(v) {

    private val mProgressHandler by lazy {
        ReactdroidProgressBarWatcher.create(mView) { progress ->
            //assume state already exists
            setState(this.ownState.cloneWithNewProgress(progress))
        }
    }

    final override fun UNSAFE_componentDidUpdateHint(prevProps: P, prevState: S, snapshot: Any?) {
        super.UNSAFE_componentDidUpdateHint(prevProps, prevState, snapshot)

        //props may have changed, if so, we update our state for a re-render that will actually affect
        // the view
        if (this.props.progress != prevProps.progress) {
            setState(this.ownState.cloneWithNewProgress(this.props.progress))
        }
    }

    override fun render() {

        //update min/max
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            props.min?.let {
                mView.min = it
            }

            props.max?.let {
                mView.max = it
            }
        }

        //update progress.
        // If change came from props, this will do nothing as we use state value.
        // But didUpdate callback will setState and we will re-render again.
        // Both methods below check for actual value change so it's safe
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mView.setProgress(ownState.progress, props.animateChanges)
        } else {
            mView.progress = ownState.progress
        }
    }
}