package com.guymichael.reactdroid.extensions.navigation.model

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    ActivityForegroundState.CREATED,
    ActivityForegroundState.STARTED,
    ActivityForegroundState.RESUMED
    ,
    ActivityForegroundState.PAUSED,
    ActivityForegroundState.STOPPED,
    ActivityForegroundState.DESTROYED
    ,
    ActivityForegroundState.NEVER_CREATED
)
annotation class ActivityForegroundState {
    companion object {
        const val INITIALIZED = 0
        const val CREATED = 1
        const val STARTED = 2
        const val RESUMED = 3
        const val PAUSED = 4
        const val STOPPED = 5
        const val DESTROYED = 6

        const val NEVER_CREATED = -1
    }
}

fun nameFor(state: @ActivityForegroundState Int): String {
    return when(state) {
        ActivityForegroundState.INITIALIZED -> "INITIALIZED"
        ActivityForegroundState.CREATED -> "CREATED"
        ActivityForegroundState.STARTED -> "STARTED"
        ActivityForegroundState.RESUMED -> "RESUMED"
        ActivityForegroundState.PAUSED -> "PAUSED"
        ActivityForegroundState.STOPPED -> "STOPPED"
        ActivityForegroundState.DESTROYED -> "DESTROYED"
        ActivityForegroundState.NEVER_CREATED -> "NEVER_CREATED"
        else -> "Unknown state number: $state"
    }
}