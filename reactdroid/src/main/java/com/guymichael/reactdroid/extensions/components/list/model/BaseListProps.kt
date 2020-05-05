package com.guymichael.reactdroid.extensions.components.list.model

import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseListProps(
        open val items: List<ListItemProps>
    ) : OwnProps() {

    override fun getAllMembers(): List<*> = items
}