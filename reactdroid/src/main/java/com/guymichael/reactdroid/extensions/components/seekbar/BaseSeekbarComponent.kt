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
                callbackDebounceDisposable?.dispose()

                props.initial_progressCallbackDebounceMs?.also {
                    callbackDebounceDisposable = APromise.delay(it) {
                        props.progress.second.invoke(progress)
                    }

                } ?: props.progress.second.invoke(progress) //if no debounce
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
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