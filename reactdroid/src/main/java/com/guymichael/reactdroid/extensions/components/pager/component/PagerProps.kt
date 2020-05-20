package com.guymichael.reactdroid.extensions.components.pager.component

import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.extensions.components.pager.component.adapter.PagerItemProps

data class PagerProps(
        val pages: List<PagerItemProps<*>>
    ) : OwnProps() {

    override fun getAllMembers() = pages
}