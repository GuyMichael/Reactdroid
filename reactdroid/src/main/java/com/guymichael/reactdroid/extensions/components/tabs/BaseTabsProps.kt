package com.guymichael.reactdroid.extensions.components.tabs

import com.guymichael.kotlinreact.model.OwnProps

abstract class BaseTabsProps : OwnProps() {

    override fun getAllMembers() = getExtraMembers()

    abstract fun getExtraMembers(): List<*>
}