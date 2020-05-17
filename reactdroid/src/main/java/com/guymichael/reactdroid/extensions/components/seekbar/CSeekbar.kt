package com.guymichael.reactdroid.extensions.components.seekbar

import android.app.Activity
import android.view.View
import android.widget.SeekBar
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

class CSeekbar(v: SeekBar)
: BaseSeekbarComponent<SimpleSeekbarProps, EmptyOwnState, SeekBar>(v) {
    override fun createInitialState(props: SimpleSeekbarProps) = EmptyOwnState
}

fun AComponent<*, *, *>.withSeekbar(@IdRes id: Int) = CSeekbar(mView.findViewById(id))
fun View.withSeekbar(@IdRes id: Int) = CSeekbar(findViewById(id))
fun Activity.withSeekbar(@IdRes id: Int) = CSeekbar(findViewById(id))