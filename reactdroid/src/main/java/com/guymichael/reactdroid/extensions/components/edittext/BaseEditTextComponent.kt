package com.guymichael.reactdroid.extensions.components.edittext

import android.widget.EditText
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState

abstract class BaseEditTextComponent<I : Any, P : OwnProps, S : OwnState, V : EditText>(v: V)
    : AComponent<P, S, V>(v), ReactdroidTextWatcher.ReactdroidTextWatcherListener<I> {

    private val mTextHandler: ReactdroidTextWatcher<I> by lazy {
        ReactdroidTextWatcher.create(mView, this)
    }

    protected abstract fun getValue(props: P): I?
    protected abstract fun parseOrThrow(rawValue: String): I

    final override fun parseValueOrThrow(rawValue: String?): I? {
        return rawValue?.takeIf { it.isNotBlank() }?.let(::parseOrThrow)
    }

    override fun render() {
        mTextHandler.setValue(getValue(this.props))
    }
}