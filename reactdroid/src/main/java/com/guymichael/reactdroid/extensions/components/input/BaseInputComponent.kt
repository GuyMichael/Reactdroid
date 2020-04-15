package com.guymichael.reactdroid.extensions.components.input

import android.widget.EditText
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.reactdroid.model.ReactTextWatcher
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState

abstract class BaseInputComponent<I : Any, P : OwnProps, S : OwnState, V : EditText>(v: V)
    : AComponent<P, S, V>(v), ReactTextWatcher.ReactTextWatcherListener<I> {

    private val mTextHandler: ReactTextWatcher<I> by lazy {
        ReactTextWatcher.create(mView, this)
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