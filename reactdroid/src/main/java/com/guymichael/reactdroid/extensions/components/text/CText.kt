package com.guymichael.reactdroid.extensions.components.text

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.model.AComponent

class CText(v: TextView) : BaseATextComponent<ATextProps, EmptyOwnState, TextView>(v) {
    override fun createInitialState(props: ATextProps) = EmptyOwnState
}

//THINK as Annotations
fun withText(textView: TextView) = CText(textView)
fun View.withText(@IdRes id: Int) = CText(findViewById(id))
fun AComponent<*, *, *>.withText(@IdRes id: Int) = CText(mView.findViewById(id))