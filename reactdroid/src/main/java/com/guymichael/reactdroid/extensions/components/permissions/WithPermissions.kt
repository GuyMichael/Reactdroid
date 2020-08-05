package com.guymichael.reactdroid.extensions.components.permissions

import android.view.View
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.ownstate.BooleanState
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.autoCancel
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.core.model.AHOC
import com.guymichael.reactdroid.extensions.animation.renderOrGone

class WithPermissions<COMPONENT_PROPS : OwnProps, V : View, C : AComponent<COMPONENT_PROPS, *, V>>(
        component: C
    ) : AHOC<PermissionProps<COMPONENT_PROPS>, COMPONENT_PROPS, V, C, BooleanState>(
        component, false
    ) {

    override fun createInitialState(props: PermissionProps<COMPONENT_PROPS>) = BooleanState(checkPermissions(props))
    override fun mapToComponentProps(hocProps: PermissionProps<COMPONENT_PROPS>) = hocProps.componentProps

    override fun componentDidMount() {
        if( !ownState.value) {
            //permissions not granted according to state
            PermissionsLogic.requestPermissions(
                    Utils.getActivity(mView.context, ComponentActivity::class.java)!!
                    , props.permissions
                    , props.initial_goToAppSettingsIfAlwaysDeny
                )
                .then {
                    setState(BooleanState(true))
                }
                .catch { e ->
                    //e is (probably) a PermissionsDeniedException
                    props.initial_actionOnDenied.invoke(e, this)
                }
                .autoCancel(this)
                .execute()
        }
    }

    override fun componentDidUpdate(prevProps: PermissionProps<COMPONENT_PROPS>
                                    , prevState: BooleanState, snapshot: Any?) {

        //update state if requested permissions changed
        if (this.props.permissions != prevProps.permissions) {
            this.ownState.takeIf {
                it.value != checkPermissions(this.props)
            }?.also {
                setState(BooleanState(!it.value))
            }
        }
    }





    private fun checkPermissions(props: PermissionProps<COMPONENT_PROPS> = this.props): Boolean {
        return PermissionsLogic.checkPermissions(mView.context, props.permissions.toTypedArray())
    }




    override fun render() {
        mComponent.renderOrGone(
            props.componentProps.takeIf { ownState.value /*permissions granted when true*/ }
            , animateVisibility = props.initial_animateVisibility
            , animDuration = 250
        )
    }
}





fun <COMPONENT_PROPS : OwnProps, V : View, C : AComponent<COMPONENT_PROPS, *, V>>
withPermissions(component: C) = WithPermissions(
    component
)
