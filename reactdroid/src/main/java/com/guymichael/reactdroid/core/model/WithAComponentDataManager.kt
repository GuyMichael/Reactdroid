package com.guymichael.reactdroid.core.model

import android.view.View
import com.guymichael.kotlinreact.model.ComponentDataManager
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.WithComponentDataManager

class WithAComponentDataManager<P : OwnProps, DATA_PROPS : OwnProps, V : View
        , C : AComponent<P, *, V>, D : ComponentDataManager<P, DATA_PROPS>>(
        component: C
        , override val dataManager: D
) : AHOC<P, P, V, C>(component)
    , WithComponentDataManager<P, DATA_PROPS, C, D> {

    override fun mapToComponentProps(hocProps: P) = hocProps //no op
}