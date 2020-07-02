package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.model.AComponent

class CBtn(v: Button) : BaseTextComponent<TextProps, EmptyOwnState, Button>(v) {
    override fun createInitialState(props: TextProps) = EmptyOwnState
}

//THINK as Annotations
fun withBtn(textView: Button) = CBtn(textView)
fun AComponent<*, *, *>.withBtn(@IdRes id: Int) = CBtn(mView.findViewById(id))
fun View.withBtn(@IdRes id: Int) = CBtn(findViewById(id))
fun Activity.withBtn(@IdRes id: Int) = CBtn(findViewById(id))