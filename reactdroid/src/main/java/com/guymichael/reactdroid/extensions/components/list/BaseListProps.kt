package com.guymichael.reactdroid.extensions.components.list

import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.extensions.list.model.ListItemProps

abstract class BaseListProps(
        open val items: List<ListItemProps>
    ) : OwnProps() {

    override fun getAllMembers(): List<*> = items
}