package com.guymichael.reactdroid.extensions.components.list.model

import com.guymichael.kotlinreact.model.OwnState


data class ListState(
        val uncontrolledScrollIndex: Int? = null //doesn't cause re-renders
    ) : OwnState() {

    override fun getAllMembers() = emptyList<Any?>()
}