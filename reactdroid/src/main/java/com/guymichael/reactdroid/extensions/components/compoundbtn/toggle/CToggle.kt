package com.guymichael.reactdroid.extensions.components.compoundbtn.toggle

import android.widget.ToggleButton
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.compoundbtn.BaseCompoundBtnComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
class CToggle(v: ToggleButton) : BaseCompoundBtnComponent<ToggleProps, EmptyOwnState, ToggleButton>(v) {
    override fun createInitialState(props: ToggleProps) = EmptyOwnState
    override fun renderExtra() {}
}




fun AComponent<*, *, *>.withToggle(@IdRes id: Int) = CToggle(mView.findViewById(id))