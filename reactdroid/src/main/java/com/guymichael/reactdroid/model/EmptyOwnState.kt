package com.guymichael.reactdroid.model

/** Use when you don't want to handle own state, meaning, your Component solely use props, either from state or 'apiProps' from a parent Component */
object EmptyOwnState : OwnState() {
    override fun getAllMembers(): List<*> = emptyList<Any>()
}