package com.guymichael.reactdroid.extensions.components.list.model

import com.guymichael.kotlinreact.model.OwnState


data class ListState(
        val uncontrolledScrollIndex: Int? = null //doesn't cause re-renders
    ) : OwnState() {

    fun cloneWithNewScroll(uncontrolledScrollIndex: Int): ListState {
        return ListState(
            uncontrolledScrollIndex = uncontrolledScrollIndex
        )
    }

    override fun getAllMembers() = emptyList<Any?>()
}