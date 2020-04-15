package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState

class CTextLocale(v: LinearLayout) : BaseLocaleText<ATextProps, EmptyOwnState, CText>(v) {
    override fun createInitialState(props: ATextProps) = EmptyOwnState
    override fun mapTextToProps(text: CharSequence) = ATextProps(text)
    override fun onBindTextView(view: TextView) = withText(view)
}


//THINK as Annotations
fun withLocaleText(v: LinearLayout) = CTextLocale(v)
fun View.withLocaleText(@IdRes id: Int) = CTextLocale(findViewById(id))
fun Activity.withLocaleText(@IdRes id: Int) = CTextLocale(findViewById(id))