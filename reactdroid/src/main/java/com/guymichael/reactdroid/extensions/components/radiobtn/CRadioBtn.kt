package com.guymichael.reactdroid.extensions.components.radiobtn

import android.widget.RadioButton
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.lib.reactdroid.components.compoundbtn.BaseCompoundBtnComponent
import com.guymichael.reactdroid.core.model.AComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
class CRadioBtn(v: RadioButton) : BaseCompoundBtnComponent<RadioBtnProps, EmptyOwnState, RadioButton>(v) {
    override fun createInitialState(props: RadioBtnProps) = EmptyOwnState
    override fun renderExtra() {}
}




fun AComponent<*, *, *>.withRadioBtn(@IdRes id: Int) = CRadioBtn(mView.findViewById(id))