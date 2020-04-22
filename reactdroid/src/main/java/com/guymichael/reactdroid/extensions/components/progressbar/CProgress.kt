package com.guymichael.reactdroid.extensions.components.progressbar

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.IdRes
import com.guymichael.reactdroid.model.AComponent

class CProgress(v: ProgressBar)
: BaseProgressComponent<SimpleProgressProps, SimpleProgressOwnState, ProgressBar >(v) {

    override fun createInitialState(props: SimpleProgressProps): SimpleProgressOwnState {
        return SimpleProgressOwnState(props.progress)
    }
}

fun withProgress(v: ProgressBar) = CProgress(v)
fun AComponent<*, *, *>.withProgress(@IdRes id: Int) = CProgress(mView.findViewById(id))
fun View.withProgress(@IdRes id: Int) = CProgress(findViewById(id))
fun Activity.withProgress(@IdRes id: Int) = CProgress(findViewById(id))