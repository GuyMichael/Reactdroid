package com.guymichael.kotlinreact

import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.kotlinreact.model.ownstate.BooleanState
import com.guymichael.kotlinreact.model.props.BooleanProps

fun <S : OwnState> assertOwnState(component: Component<*, S>, expect: S
        , customLazyLog: (() -> Unit)? = null) {

    assert(component.ownState == expect, customLazyLog ?: {
        println("component's ownState differs from what was expected")
        println("component's actual ownState: ${component.ownState}")
        println("component's expected ownState: $expect")
    })
}

fun assertOwnState(component: Component<*, BooleanState>, expect: Boolean
        , customLazyLog: (() -> Unit)? = null) {

    assertOwnState(component, BooleanState(expect), customLazyLog)
}

fun <P : OwnProps> assertProps(component: Component<P, *>, expect: P
        , customLazyLog: (() -> Unit)? = null) {

    assert(component.props == expect, customLazyLog ?: {
        println("component's props differs from what was expected")
        println("component's actual props: ${component.props}")
        println("component's expected props: $expect")
    })
}

fun assertProps(component: Component<BooleanProps, *>, expect: Boolean
                   , customLazyLog: (() -> Unit)? = null) {

    assertProps(component, BooleanProps(expect), customLazyLog)
}