package com.guymichael.reactdroid.extensions.components.input

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.guymichael.reactdroid.extensions.components.text.ReactdroidTextHelper
import java.lang.ref.WeakReference
import java.text.ParseException

class ReactdroidTextWatcher<T> private constructor(
        private val et: WeakReference<EditText>
        , private val listener: ReactdroidTextWatcherListener<T>
    ): TextWatcher {

    private var prevText: String? = et.get()?.text?.toString()

    fun setValue(value: T?) {
        et.get()?.let {
            it.removeTextChangedListener(this)
            ReactdroidTextHelper.setText(
                it,
                listener.formatValue(value)
            )
            it.addTextChangedListener(this)
        }
    }

    private fun setValue(value: T?, forUserInput: String?) {
        et.get()?.let {
            it.removeTextChangedListener(this)
            ReactdroidTextHelper.setText(
                it,
                listener.formatValue(value, forUserInput)
            )
            it.addTextChangedListener(this)
        }
    }

    fun setText(s: CharSequence?) {
        et.get()?.let {
            it.removeTextChangedListener(this)
            ReactdroidTextHelper.setText(
                it,
                s
            )
            it.addTextChangedListener(this)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        prevText = s?.toString()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        try {
            val userInput: String? = s?.toString()
            val newValue = listener.parseValueOrThrow(userInput)//may throw ParseException

            //if didn't throw, newValue is approved
            setValue(newValue, userInput)

            //notify listener
            listener.onChanged(newValue)

        } catch(e : ParseException) {
            //restore text to it's prev value without notifying
            setText(prevText)
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    companion object {
        @JvmStatic
        fun <S> create(et: EditText, listener: ReactdroidTextWatcherListener<S>) : ReactdroidTextWatcher<S> {
            val watcher =
                ReactdroidTextWatcher(
                    WeakReference(et),
                    listener
                )
            et.addTextChangedListener(watcher)
            return watcher
        }

        fun create(et: EditText, listener: (String?) -> Unit) : ReactdroidTextWatcher<String?> {

            val watcher =
                ReactdroidTextWatcher(
                    WeakReference(et),
                    object :
                        ReactdroidSimpleTextWatcherListener {
                        override fun onChanged(value: String?) {
                            listener(value)
                        }
                    })
            et.addTextChangedListener(watcher)
            return watcher
        }
    }

    interface ReactdroidTextWatcherListener<R> {
        /** Called off the main thread */
        fun onChanged(value: R?)
        /** @throws ParseException */
        @Throws(ParseException::class)
        fun parseValueOrThrow(rawValue: String?) : R?//throws ParseException
        fun formatValue(value: R?, forUserInput: String? = null) : CharSequence?
    }

    interface ReactdroidSimpleTextWatcherListener:
        ReactdroidTextWatcherListener<String?> {
        override fun parseValueOrThrow(rawValue: String?) = rawValue
        override fun formatValue(value: String?, forUserInput: String?) = value
    }
}