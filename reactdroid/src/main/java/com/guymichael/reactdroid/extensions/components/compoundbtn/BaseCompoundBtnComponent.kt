package com.guymichael.lib.reactdroid.components.compoundbtn

import android.view.View
import android.widget.CompoundButton
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.core.model.android.DebouncedClickListener
import com.guymichael.reactdroid.extensions.components.compoundbtn.BaseCompoundBtnProps

/** Fully controlled component - must receive props for `current state` and user-change `callback` */
abstract class BaseCompoundBtnComponent<P : BaseCompoundBtnProps, S : OwnState, V : CompoundButton>(
        v: V
    ) : AComponent<P, S, V>(v) {

    init {
        //connect view clicks to new-state (isChecked) dispatcher
        mView.setOnClickListener(object : DebouncedClickListener(150) {
            override fun onClicked(v: View) {
                //notify props callback, which in return should change the first (Boolean) props param
                onViewClicked(mView.isChecked, props.checked.second)
            }
        })
    }

    /** This is where you should pass `checked` to the props' callback - `dispatcher` */
    protected open fun onViewClicked(checked: Boolean, dispatcher: (Boolean) -> Unit) {
        dispatcher.invoke(checked)
    }

    final override fun render() {
        mView.isChecked = props.checked.first //setChecked already checks for diff

        props.text.also { if (it != mView.text) {
            mView.text = it
        }}

        renderExtra()
    }

    /** Called after the default [render], this is where you should render any extra effects */
    abstract fun renderExtra()
}