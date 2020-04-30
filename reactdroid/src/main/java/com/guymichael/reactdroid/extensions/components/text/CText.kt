package com.guymichael.reactdroid.extensions.components.text

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

class CText(v: TextView) : BaseTextComponent<TextProps, EmptyOwnState, TextView>(v) {
    override fun createInitialState(props: TextProps) = EmptyOwnState
}

//THINK as Annotations
fun withText(textView: TextView) = CText(textView)
fun View.withText(@IdRes id: Int) = CText(findViewById(id))
fun AComponent<*, *, *>.withText(@IdRes id: Int) = CText(mView.findViewById(id))