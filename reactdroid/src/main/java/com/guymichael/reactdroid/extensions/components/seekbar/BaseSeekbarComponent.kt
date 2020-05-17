package com.guymichael.reactdroid.extensions.components.seekbar

import android.os.Build
import android.widget.SeekBar
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent
import io.reactivex.rxjava3.disposables.Disposable

/** A component which can be both controlled and uncontrolled -
 * Depending on the nullability of its props' `progress` argument,
 * which represents `current progress` and on-user-change `callback` */
abstract class BaseSeekbarComponent<P : BaseSeekbarProps, S : OwnState, V : SeekBar>(
        v: V
    ) : AComponent<P, S, V>(v) {

    private var callbackDebounceDisposable: Disposable? = null

    init {
        mView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //notify progress callback
                when (val debounce = props.initial_progressCallbackDebounceMs?.takeIf { it > 0 }) {
                    // no debounce
                    null -> props.progress.second.invoke(progress) //call back now

                    //debounce set to 'touch stop'
                    Long.MAX_VALUE -> {} //no op, see onStopTrackingTouch

                    //normal debounce
                    else -> {
                        callbackDebounceDisposable?.dispose()
                        callbackDebounceDisposable = APromise.delayWhileAlive(mView, debounce) {
                            props.progress.second.invoke(progress)
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //notify progress callback if debounce set to 'on touch up'
                props.initial_progressCallbackDebounceMs?.takeIf { it == Long.MAX_VALUE}?.also {
                    props.progress.second.invoke(mView.progress)
                }
            }
        })
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
        props.progress.first.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mView.setProgress(it, props.initial_animateChanges)
            } else {
                mView.progress = it
            }
        }
    }
}