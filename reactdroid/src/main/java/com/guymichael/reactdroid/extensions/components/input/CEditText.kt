package com.guymichael.reactdroid.extensions.components.input

import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.EmptyOwnState
import java.text.ParseException

class CEditText<I : Any>(
        v: EditText
        , private val onChange: (I?) -> Unit
        , private val parseValueOrThrow: (String) -> I
        , private val formatter: (I?, forUserInput: String?) -> CharSequence?
    ) : BaseEditTextComponent<I, InputProps<I>, EmptyOwnState, EditText>(v) {

    override fun createInitialState(props: InputProps<I>) = EmptyOwnState
    override fun getValue(props: InputProps<I>) = props.value
    override fun onChanged(value: I?) = onChange.invoke(value)
    override fun parseOrThrow(rawValue: String) = parseValueOrThrow.invoke(rawValue)
    override fun formatValue(value: I?, forUserInput: String?) = formatter.invoke(value, forUserInput)

    fun onRender(value: I?) {
        onRender(InputProps(value))
    }
}


fun View.withStringInput(@IdRes editText: Int, onChange: (String?) -> Unit) : CEditText<String> {
    return CEditText(findViewById(editText), onChange, { it }, { value, _ -> value })
}

fun View.withLongInput(@IdRes editText: Int, onChange: (Long?) -> Unit
        , parseValueOrThrow: (String) -> Long
        , formatter: (Long?, forUserInput: String?) -> CharSequence?
        , valueValidator: (Long) -> Boolean = { true }) : CEditText<Long> {


    return CEditText(findViewById(editText), onChange, {
        parseOrThrow(it, parseValueOrThrow, valueValidator)
    }, formatter)
}




private fun <T : Any> parseOrThrow(rawValue: String
        , parser: (String) -> T
        , validator: (T) -> Boolean): T {

    return parser(rawValue).let {
        if (validator(it)) {
            it
        } else {
            throw ParseException("invalid input value $rawValue", 0)
        }
    }
}