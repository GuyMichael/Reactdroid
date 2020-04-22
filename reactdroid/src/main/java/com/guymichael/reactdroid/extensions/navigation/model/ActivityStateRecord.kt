package com.guymichael.reactdroid.extensions.navigation.model

import android.app.Activity
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import java.lang.ref.WeakReference

internal data class ActivityStateRecord private constructor(
    val activity: WeakReference<out Activity>?
    , val state: @ActivityForegroundState Int = ActivityForegroundState.NEVER_CREATED
    , val page: ClientPageIntf?
) {

    /** Considered open if activity is alive (not destroyed or finishing)
     * and state is `CREATED` to `STOPPED`, inclusive */
    fun isPageAlive(): Boolean {
        return isActivityAlive() && (
            state == ActivityForegroundState.CREATED
            || state == ActivityForegroundState.STARTED
            || state == ActivityForegroundState.RESUMED
            || state == ActivityForegroundState.PAUSED
            || state == ActivityForegroundState.STOPPED
        )
    }

    fun isActivityAlive(): Boolean {
        return activity?.get()?.let {
            !it.isDestroyed && !it.isFinishing
        } ?: false
    }

    companion object {
        fun from(activity: Activity?
                , state: @ActivityForegroundState Int = ActivityForegroundState.NEVER_CREATED
                , page: ClientPageIntf?
            ): ActivityStateRecord? {

            return ActivityStateRecord(activity?.let(::WeakReference), state, page)
        }
    }
}