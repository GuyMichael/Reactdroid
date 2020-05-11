package com.guymichael.reactdroid.core.model

import android.view.View
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.HOC
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState

abstract class
AHOC<HOC_PROPS : OwnProps, COMPONENT_PROPS : OwnProps, V : View, C : AComponent<COMPONENT_PROPS, *, V>, S : OwnState>(
        override val mComponent: C
        , reRenderOnRemount: Boolean = false
    ) : HOC<HOC_PROPS, COMPONENT_PROPS, C, S>
    , AComponent<HOC_PROPS, S, V>(mComponent.mView, reRenderOnRemount) {

    companion object {
        @JvmStatic
        fun <HOC_PROPS : OwnProps, COMPONENT_PROPS : OwnProps, V : View> from(
            component: AComponent<COMPONENT_PROPS, *, V>
            , mapToInnerProps: (HOC_PROPS) -> COMPONENT_PROPS
        ): AComponent<HOC_PROPS, *, *> {

            return object
                : AHOC<HOC_PROPS, COMPONENT_PROPS, V, AComponent<COMPONENT_PROPS, *, V>, EmptyOwnState>(
                    component
                ) {
                override fun createInitialState(props: HOC_PROPS) = EmptyOwnState
                override fun mapToComponentProps(hocProps: HOC_PROPS) = mapToInnerProps.invoke(hocProps)
            }
        }
    }
}