package com.guymichael.reactdroid.extensions.components.edittext

import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import com.google.android.material.textfield.TextInputLayout
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.ViewUtils
import com.guymichael.reactdroid.core.model.AComponent
import java.lang.ref.WeakReference
import java.text.ParseException

class CEditText<I : Any>(
        v: EditText
        , private val onChange: (I?) -> Unit
        , private val parseValueOrThrow: (String) -> I
        , private val formatter: (I?, forUserInput: String?) -> CharSequence?
    ) : BaseEditTextComponent<I, EditTextProps<I>, EmptyOwnState, EditText>(v) {

    private var textInputParentRef: WeakReference<out TextInputLayout>? = null

    override fun createInitialState(props: EditTextProps<I>) = EmptyOwnState
    override fun getValue(props: EditTextProps<I>) = props.value
    override fun onChanged(value: I?) = onChange.invoke(value)
    override fun parseOrThrow(rawValue: String) = parseValueOrThrow.invoke(rawValue)
    override fun formatValue(value: I?, forUserInput: String?) = formatter.invoke(value, forUserInput)

    override fun render() {
        //value
        super.render()

        //error
        props.error.also { error ->
        getAndCacheTextInputParent()?.takeIf { it.error != error }?.also {
            it.error = error
        }}
        //THINK if no TextInputLayout parent, set EditText's text & color to "error"
    }

    /* API */
    fun onRender(value: I?, error: CharSequence? = null) {
        onRender(EditTextProps(value, error))
    }

    /* Privates */
    private fun getAndCacheTextInputParent(): TextInputLayout? {
        return this.textInputParentRef?.get()
            ?: ViewUtils.findParent(mView, TextInputLayout::class, 3)?.also {
                this.textInputParentRef = WeakReference(it)
                //last time checked, it took 2 iterations to find
            }
    }
}


fun View.withStringInput(@IdRes editText: Int
        , onChange: (String?) -> Unit
        , formatter: (String?) -> CharSequence? = { it }
    ) : CEditText<String> {

    return CEditText(findViewById(editText), onChange, { it }, { value, _ -> formatter(value) })
}

fun View.withLongInput(@IdRes editText: Int, onChange: (Long?) -> Unit
        , parseValueOrThrow: (String) -> Long
        , formatter: (Long?, forUserInput: String?) -> CharSequence?
        , valueValidator: (Long) -> Boolean = { true }
    ) : CEditText<Long> {

    return CEditText(findViewById(editText), onChange, {
        parseOrThrow(it, parseValueOrThrow, valueValidator)
    }, formatter)
}

fun View.withDoubleInput(@IdRes editText: Int, onChange: (Double?) -> Unit
        , parseValueOrThrow: (String) -> Double
        , formatter: (Double?, forUserInput: String?) -> CharSequence?
        , valueValidator: (Double) -> Boolean = { true }
    ) : CEditText<Double> {

    return CEditText(findViewById(editText), onChange, {
        parseOrThrow(it, parseValueOrThrow, valueValidator)
    }, formatter)
}

fun AComponent<*, *, *>.withStringInput(@IdRes editText: Int, onChange: (String?) -> Unit) : CEditText<String> {
    return mView.withStringInput(editText, onChange)
}

fun AComponent<*, *, *>.withLongInput(@IdRes editText: Int, onChange: (Long?) -> Unit
        , parseValueOrThrow: (String) -> Long
        , formatter: (Long?, forUserInput: String?) -> CharSequence?
        , valueValidator: (Long) -> Boolean = { true }
    ) : CEditText<Long> {

    return mView.withLongInput(editText, onChange, parseValueOrThrow, formatter, valueValidator)
}

fun AComponent<*, *, *>.withDoubleInput(@IdRes editText: Int, onChange: (Double?) -> Unit
        , parseValueOrThrow: (String) -> Double
        , formatter: (Double?, forUserInput: String?) -> CharSequence?
        , valueValidator: (Double) -> Boolean = { true }
    ) : CEditText<Double> {

    return mView.withDoubleInput(editText, onChange, parseValueOrThrow, formatter, valueValidator)
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