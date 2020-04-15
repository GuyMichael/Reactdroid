package com.guymichael.kotlinreact.model

interface WithComponentDataManager<P : OwnProps, DATA_PROPS : OwnProps
        , C : Component<P, *>, D : ComponentDataManager<P, DATA_PROPS>>
    : HOC<P, P, C> {

    val dataManager: D

    override fun componentDidMount() {
        dataManager.onComponentDidMount(this.props)
    }

    override fun componentDidUpdate(prevProps: P, prevState: EmptyOwnState, snapshot: Any?) {
        dataManager.onComponentDidUpdate(this.props, prevProps)
    }
}