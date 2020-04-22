package com.guymichael.reactdroid.extensions.components.text

import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

object ReactdroidTextHelper {
    /**
     * Uses to easily handle EditText#setText during renders, while maintaining cursor position behaviour
     */
    fun setText(editText: EditText, text: CharSequence?) {
        if(editText.text?.toString() == text?.toString()) {return}//same as DOM comparison

        val prevTextLength = editText.length()
        val nextTextLength = text?.length ?: 0
        val prevCursorPos = editText.selectionStart
//            val isPrevCursorPosAtEnd = prevCursorPos == prevTextLength
        val textLengthDiff = nextTextLength - prevTextLength

        editText.setText(text)

        //handle cursor position
        if(nextTextLength > 0) {
            editText.setSelection(max(0, min(prevCursorPos + textLengthDiff, nextTextLength)))
        }
    }
}