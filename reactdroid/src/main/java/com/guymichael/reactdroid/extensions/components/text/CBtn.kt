package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState

class CBtn(v: Button) : BaseATextComponent<ATextProps, EmptyOwnState, Button>(v) {
    override fun createInitialState(props: ATextProps) = EmptyOwnState
}

//THINK as Annotations
fun withBtn(textView: Button) = CBtn(textView)
fun View.withBtn(@IdRes id: Int) = CBtn(findViewById(id))
fun Activity.withBtn(@IdRes id: Int) = CBtn(findViewById(id))