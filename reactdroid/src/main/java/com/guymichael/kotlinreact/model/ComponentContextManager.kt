package com.guymichael.kotlinreact.model

interface ComponentContextManager<P : OwnProps, CONTEXT_PROPS : OwnState> {

    fun mapPropsToContextProps(ownProps: P) : CONTEXT_PROPS
    fun onContextPropsChanged(newProps: P, prevProps: P, nextDataProps: CONTEXT_PROPS, prevDataProps: CONTEXT_PROPS)

    fun onComponentDidMount(props: P, isRemount: Boolean) {
        //default - no op
    }

    fun onComponentDidUpdate(newProps: P, prevProps: P) {
        val nextDataProps = mapPropsToContextProps(newProps)

        mapPropsToContextProps(prevProps).let { prevDataProps ->
            if (shouldNotifyComponent(prevDataProps, nextDataProps)) {
                onContextPropsChanged(newProps, prevProps, nextDataProps, prevDataProps)
            }
        }
    }

    fun shouldNotifyComponent(prevDataProps: CONTEXT_PROPS, nextDataProps: CONTEXT_PROPS) : Boolean {
        return !prevDataProps.equals(nextDataProps)
    }

    //THINK getDisplayName() so logs can show
}