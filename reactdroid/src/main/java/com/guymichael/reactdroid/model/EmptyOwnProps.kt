package com.guymichael.reactdroid.model

/** Use when you don't want to handle own props, meaning, your Component solely use state props*/
object EmptyOwnProps : OwnProps() {
    override fun getAllMembers(): List<*> = emptyList<Any>()
}