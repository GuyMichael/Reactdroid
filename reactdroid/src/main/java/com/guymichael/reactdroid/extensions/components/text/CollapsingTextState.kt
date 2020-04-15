package com.guymichael.reactdroid.extensions.components.text

import com.guymichael.kotlinreact.model.OwnState

data class CollapsingTextState(val isCollapsed: Boolean) : OwnState() {

    override fun getAllMembers(): List<*> = listOf(
        isCollapsed
    )

    companion object {
        fun from(props: CollapsingTextProps) = CollapsingTextState(props.startCollapsed)
    }
}