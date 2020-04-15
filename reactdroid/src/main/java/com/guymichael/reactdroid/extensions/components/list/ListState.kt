package com.guymichael.reactdroid.extensions.components.list

import com.guymichael.kotlinreact.model.OwnState


data class ListState(
        val uncontrolledIndex: Int
    ) : OwnState() {

    override fun getAllMembers() = listOf(
        uncontrolledIndex
    )
}