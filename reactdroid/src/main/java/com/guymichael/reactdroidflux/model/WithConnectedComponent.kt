package com.guymichael.reactdroidflux.model

import android.view.View
import com.guymichael.kotlinflux.model.ConnectedHOC
import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.Store
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.reactdroid.model.AHOC
import com.guymichael.kotlinreact.model.OwnProps
import io.reactivex.rxjava3.disposables.Disposable

abstract class WithConnectedComponent<API_PROPS : OwnProps, COMPONENT_PROPS : OwnProps
        , V : View, C : AComponent<COMPONENT_PROPS, *, V>>(
        component: C
) : AHOC<API_PROPS, COMPONENT_PROPS, V, C>(
        component
        , true //note: we have to use 'true', because when we're not mounted,
        //      we aren't connected to the store and thus don't receive
        //      props changes at this time.
        //      So, we have to re-render, which remaps the apiProps+global-state
        //      to the inner component's props
        //THINK we could instead stay connected to the store until the actual view dies
    )
    , ConnectedHOC<API_PROPS, COMPONENT_PROPS, C> {

    final override var storeDisposable: Disposable? = null
    final override var innerComponentProps: COMPONENT_PROPS? = null
}




//export as a method
fun <API_PROPS : OwnProps, COMPONENT_PROPS : OwnProps, V : View, C : AComponent<COMPONENT_PROPS, *, V>>
connect(component: C
        , mapStateToProps: (state: GlobalState, apiProps: API_PROPS) -> COMPONENT_PROPS
        , storeSupplier: () -> Store)
    : AHOC<API_PROPS, *, V, C> {

    return object : WithConnectedComponent<API_PROPS, COMPONENT_PROPS, V, C>(component) {
        override fun getStore() = storeSupplier.invoke()
        override fun mapStateToProps(state: GlobalState, apiProps: API_PROPS) = mapStateToProps.invoke(state, apiProps)
        override fun getDisplayName() = "WithConnectedComponent(${mComponent.getDisplayName()})"
    }
}