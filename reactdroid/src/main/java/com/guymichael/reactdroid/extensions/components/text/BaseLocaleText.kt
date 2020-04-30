package com.guymichael.reactdroid.extensions.components.text

import android.widget.LinearLayout
import android.widget.TextView
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent

abstract class BaseLocaleText<P : BaseTextProps, S : OwnState, T : AComponent<P, *, TextView>>(v: LinearLayout)
        : AComponent<P, S, LinearLayout>(v) {

    protected val cText: T = onBindTextView(findTextView())

    init {
        v.orientation = LinearLayout.HORIZONTAL
    }


    fun findTextView(): TextView = mView.getChildAt(0) as TextView

    abstract fun onBindTextView(view: TextView) : T
    abstract fun mapTextToProps(text: CharSequence) : P?

    override fun render() {
        cText.UNSAFE_forceRender(props) //if we have to render, assume our cText as well (same props)
    }
}