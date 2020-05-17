package com.guymichael.reactdroid.extensions.components.compoundbtn.checkbox

import android.widget.CheckBox
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.extensions.components.compoundbtn.BaseCompoundBtnComponent
import com.guymichael.reactdroid.core.model.AComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
class CCheckbox(v: CheckBox) : BaseCompoundBtnComponent<CheckboxProps, EmptyOwnState, CheckBox>(v) {
    override fun createInitialState(props: CheckboxProps) = EmptyOwnState
    override fun renderExtra() {}
}




fun AComponent<*, *, *>.withCheckbox(@IdRes id: Int) = CCheckbox(mView.findViewById(id))