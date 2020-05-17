package com.guymichael.reactdroid.extensions.components.seekbar

import android.os.Build
import androidx.annotation.RequiresApi
import com.guymichael.kotlinreact.model.OwnProps

/**
 * @param progress first param is the 'progress' value, second is the callback for when user changes,
 * where you should update your local state (`setState`) or some global state (e.g. a `Store`) to immediately
 * reflect that change (and cause re-render for this progress component)
 * @param initial_progressCallbackDebounceMs debounce on-progress-change callbacks to skip subsequent changes,
 * e.g. when user slides the seekbar (instead of clicking it). Doesn't affect re-renders
 */
abstract class BaseSeekbarProps(
        open val progress: Pair<Int, (newProgress: Int) -> Unit>
        , @RequiresApi(Build.VERSION_CODES.O) open val min: Int?
        , @RequiresApi(Build.VERSION_CODES.O) open val max: Int?
        , @RequiresApi(Build.VERSION_CODES.N) open val initial_animateChanges: Boolean = false
        , open val initial_progressCallbackDebounceMs: Long? = 150
    ): OwnProps() {

    override fun getAllMembers(): List<*> = listOf(
        progress.first, min, max
    )
}

data class SimpleSeekbarProps(
    override val progress: Pair<Int, (newProgress: Int) -> Unit>
    , @RequiresApi(Build.VERSION_CODES.O) override val min: Int? = null
    , @RequiresApi(Build.VERSION_CODES.O) override val max: Int? = null
    , @RequiresApi(Build.VERSION_CODES.N) override val initial_animateChanges: Boolean = false
    , override val initial_progressCallbackDebounceMs: Long? = 150
): BaseSeekbarProps(progress, min, max, initial_animateChanges)