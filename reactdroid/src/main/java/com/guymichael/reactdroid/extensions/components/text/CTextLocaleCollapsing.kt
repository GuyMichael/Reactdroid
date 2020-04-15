package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState

class CTextLocaleCollapsing(v: LinearLayout) : BaseLocaleText<CollapsingTextProps, EmptyOwnState, CTextCollapsing>(v) {
    override fun createInitialState(props: CollapsingTextProps) = EmptyOwnState
    override fun mapTextToProps(text: CharSequence) = CollapsingTextProps(text)
    override fun onBindTextView(view: TextView) = withCollapsingText(view)
}


//THINK as Annotations
fun View.withCollapsingLocaleText(@IdRes id: Int) = CTextLocaleCollapsing(findViewById(id))
fun Activity.withCollapsingLocaleText(@IdRes id: Int) = CTextLocaleCollapsing(findViewById(id))