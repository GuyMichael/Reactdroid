package com.guymichael.kotlinreact.model

//NOTICE: HOC's componentWillMount is not called before the first mount/render, as it currently is
// the responsibility of 'inner' component implementation (see notifyComponentWillMount())
// and we have no way to hook into it
//Note: componentDidMount is called right after component.componentDidMount (because of render calls order)
interface HOC<HOC_PROPS : OwnProps, COMPONENT_PROPS : OwnProps, C : Component<COMPONENT_PROPS, *>>
    : Component<HOC_PROPS, EmptyOwnState> {

    val mComponent: C

    override fun createInitialState(props: HOC_PROPS) = EmptyOwnState
    override fun getDisplayName() = "${javaClass.simpleName}(${mComponent.getDisplayName()})"
    override fun onHardwareBackPressed() = mComponent.onHardwareBackPressed()

    fun mapToComponentProps(hocProps: HOC_PROPS): COMPONENT_PROPS

    override fun render() {
        mComponent.onRender(mapToComponentProps(this.props))
    }
}