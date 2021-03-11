package com.guymichael.kotlinreact

import kotlin.reflect.KClass

fun <E : Throwable> assertThrows(cls: KClass<E>, statement: () -> Unit
        , customLazyLog: (() -> Unit)? = null) {

    var thrown = false

    try {
        statement.invoke()
    } catch (e: Throwable) {
        if (cls.isInstance(e)) {
            thrown = true
        } else {
            //re-throw
            throw e
        }
    }

    assert(thrown, customLazyLog ?: {
        println("statement was expected to throw a Throwable of type ${cls.simpleName} but " +
                "none was thrown")
    })
}