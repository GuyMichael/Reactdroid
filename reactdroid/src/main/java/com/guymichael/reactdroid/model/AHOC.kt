package com.guymichael.reactdroid.model

import android.view.View
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.HOC
import com.guymichael.kotlinreact.model.OwnProps

abstract class AHOC<HOC_PROPS : OwnProps, COMPONENT_PROPS : OwnProps, V : View, C : AComponent<COMPONENT_PROPS, *, V>>(
        override val mComponent: C
        , reRenderOnRemount: Boolean = false
    ) : HOC<HOC_PROPS, COMPONENT_PROPS, C>
    , AComponent<HOC_PROPS, EmptyOwnState, V>(mComponent.mView, reRenderOnRemount)