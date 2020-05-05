package com.guymichael.reactdroid.extensions.components.list.model

import com.guymichael.reactdroid.extensions.components.list.model.BaseListProps
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps

data class ListProps(
        override val items: List<ListItemProps>
        , val controlledScroll: Pair<Int, (Int) -> Unit>? = null
        , val uncontrolled_initialScrollIndex: Int = 0
    ) : BaseListProps(items) {

    override fun getAllMembers() = listOf(
        items, controlledScroll?.first
    )
}