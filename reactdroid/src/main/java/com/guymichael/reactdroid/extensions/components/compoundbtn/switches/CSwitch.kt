package com.guymichael.reactdroid.extensions.components.compoundbtn.switches

import android.widget.Switch
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.compoundbtn.BaseCompoundBtnComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
class CSwitch(v: Switch) : BaseCompoundBtnComponent<SwitchProps, EmptyOwnState, Switch>(v) {
    override fun createInitialState(props: SwitchProps) = EmptyOwnState
    override fun renderExtra() {}
}




fun AComponent<*, *, *>.withSwitch(@IdRes id: Int) = CSwitch(mView.findViewById(id))