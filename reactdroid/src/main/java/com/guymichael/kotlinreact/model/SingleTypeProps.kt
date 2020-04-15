package com.guymichael.kotlinreact.model

data class SingleTypeProps<T : Any>(val t: T?) : OwnProps() {
    override fun getAllMembers() = listOf(t)

    fun getOrNull(): T? = t
}