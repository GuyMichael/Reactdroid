package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.TestComponent

fun assertMounted(component: Component<*, *>, customLazyLog: (() -> Unit)? = null) {
    assert(component.isMounted(), customLazyLog ?: {
        println("component is not-mounted while mounted was expected")
    })
}

fun assertNotMounted(component: Component<*, *>, customLazyLog: (() -> Unit)? = null) {
    assert( !component.isMounted(), customLazyLog ?: {
        println("component is mounted while dismounted was expected")
    })
}

internal fun assertRenderCount(component: TestComponent<*, *>, expect: Int, customLazyLog: (() -> Unit)? = null) {
    assert(component.renderCount == expect, customLazyLog ?: {
        println("component is rendered ${component.renderCount} times instead the expected $expect")
    })
}

internal fun assertDidMountCount(component: TestComponent<*, *>, expect: Int, customLazyLog: (() -> Unit)? = null) {
    assert(component.didMountCount == expect, customLazyLog ?: {
        println("component's didMount was called ${component.didMountCount} times instead the expected $expect")
    })
}

internal fun assertWillUnmountCount(component: TestComponent<*, *>, expect: Int, customLazyLog: (() -> Unit)? = null) {
    assert(component.willUnmountCount == expect, customLazyLog ?: {
        println("component's willUnmount was called ${component.willUnmountCount} times instead the expected $expect")
    })
}