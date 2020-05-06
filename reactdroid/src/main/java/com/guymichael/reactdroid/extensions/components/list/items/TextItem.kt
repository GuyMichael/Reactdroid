package com.guymichael.reactdroid.extensions.components.list.items

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.reactdroid.core.model.ASimpleComponent
import com.guymichael.reactdroid.extensions.components.list.model.DataItemProps
import com.guymichael.reactdroid.extensions.components.text.renderText
import com.guymichael.reactdroid.extensions.components.text.withText

class TextItem(v: View, textView: TextView) : ASimpleComponent<DataItemProps<CharSequence>>(v) {

    private val cTxt = withText(textView)

    constructor(v: View, @IdRes textViewRes: Int)
    : this(v, v.findViewById<TextView>(textViewRes))

    @Throws(ClassCastException::class)
    constructor(v: View)
    : this(v, v as TextView)

    override fun render() {
        cTxt.renderText(props.data)
    }
}