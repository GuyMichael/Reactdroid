package com.guymichael.reactdroid.extensions.components.list.model

data class ListProps(
        override val items: List<ListItemProps<*>>
        , val constraint: String? = null
        , val controlledScroll: Pair<Int, (Int) -> Unit>? = null
        , val initialScrollIndex: Int? = null //doesn't affect re-renders
    ) : BaseListProps(items) {

    override fun getAllMembers() = listOf(
        items, controlledScroll?.first, constraint
    )
}