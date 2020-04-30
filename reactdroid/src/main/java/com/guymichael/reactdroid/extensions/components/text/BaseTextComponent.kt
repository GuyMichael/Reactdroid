package com.guymichael.reactdroid.extensions.components.text

import android.widget.TextView
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent

abstract class BaseTextComponent<P : BaseTextProps, S : OwnState, V : TextView>(v: V)
    : AComponent<P, S, V>(v) {

    override fun render() {
        mView.text = props.text
    }
}