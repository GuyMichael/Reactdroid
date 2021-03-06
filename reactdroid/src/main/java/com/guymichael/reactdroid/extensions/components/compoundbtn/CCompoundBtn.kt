package com.guymichael.reactdroid.extensions.components.compoundbtn

import android.widget.CompoundButton
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
open class CCompoundBtn(v: CompoundButton) : BaseCompoundBtnComponent<CompoundBtnProps, EmptyOwnState, CompoundButton>(v) {
    override fun createInitialState(props: CompoundBtnProps) = EmptyOwnState
    override fun renderExtra() {}
}



fun AComponent<*, *, *>.withCompoundBtn(@IdRes id: Int) = CCompoundBtn(mView.findViewById(id))