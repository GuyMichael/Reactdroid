package com.guymichael.reactdroid.extensions.components.progressbar

import com.guymichael.kotlinreact.model.OwnState

abstract class BaseProgressOwnState<S : BaseProgressOwnState<S>>(
        open val progress: Int
    ): OwnState() {

    abstract fun cloneWithNewProgress(newProgress: Int): S

    override fun getAllMembers(): List<*> = listOf(
        progress
    )
}

data class SimpleProgressOwnState(override val progress: Int)
    : BaseProgressOwnState<SimpleProgressOwnState>(progress) {

    override fun cloneWithNewProgress(newProgress: Int): SimpleProgressOwnState {
        return SimpleProgressOwnState(newProgress)
    }
}