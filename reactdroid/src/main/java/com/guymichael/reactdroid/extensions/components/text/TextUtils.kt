package com.guymichael.reactdroid.extensions.components.text

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.*
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.reactdroid.runNotNull
import java.lang.ref.WeakReference
import kotlin.math.min

class TextUtils { companion object {

    @RequiresApi(Build.VERSION_CODES.M)
    fun staticLayoutFrom(textView: TextView
            , text: CharSequence?
            , ellipsizeWidthPercentage: Float = 0.7F
            , maxLines: Int? = null): StaticLayout {

        val paint = TextPaint().apply {
            isAntiAlias = true
            textSize = textView.textSize
        }

        val width = (textView.parent as? View?)?.let { parent ->
            parent.width - parent.paddingLeft - parent.paddingStart
        } ?: 0

        return StaticLayout.Builder
                .obtain(text?:"", 0, text?.length?:0, paint, width)
                .setAlignment(parseTextViewAlignment(textView))
                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
                .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
                .setIncludePad(textView.includeFontPadding)
                .setEllipsizedWidth((width * ellipsizeWidthPercentage).toInt())
                .setEllipsize(TextUtils.TruncateAt.END)
                .runNotNull(maxLines) { setMaxLines(it) }
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun calculateDesiredLineCount(view: TextView, text: CharSequence?
            , forViewHeightPx: Int): Int {

        //THINK cache count
        return staticLayoutFrom(view, text)
                .getLastVisibleLine(forViewHeightPx) + 1
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun ellipsizeText(view: TextView, text: CharSequence?
            , maxLineCount: Int, endText: CharSequence? = null
            , forViewHeightPx: Int): CharSequence? {

        //THINK cache text
        return if (text.isNullOrBlank()) { text } else {

            staticLayoutFrom(view, text, maxLines = maxLineCount)
                .getEllipsizedText(view, forViewHeightPx, endText)
        }
    }

    fun getHeightByLineCount(view: TextView, desiredLineCount: Int): Int {
        return (view.lineHeight * desiredLineCount)/* +
                (view.paddingTop + view.paddingBottom)*/
    }

    //THINK layout or gravity alignment?
    fun parseTextViewAlignment(textView: TextView) : Layout.Alignment {
        return when(textView.textAlignment) {
            View.TEXT_ALIGNMENT_CENTER -> Layout.Alignment.ALIGN_CENTER

            /*View.TEXT_ALIGNMENT_TEXT_START, View.TEXT_ALIGNMENT_VIEW_START
                -> Layout.Alignment.ALIGN_NORMAL*/

            View.TEXT_ALIGNMENT_TEXT_END, View.TEXT_ALIGNMENT_VIEW_END
            -> Layout.Alignment.ALIGN_OPPOSITE

            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    fun colorize(text: CharSequence, @ColorInt color: Int): CharSequence {
        return text.takeIf { it.isNotBlank() }?.let {
            SpannableString(it).apply {
                setSpan(ForegroundColorSpan(color), 0, this.length, Spanned.SPAN_INTERMEDIATE)
            }
        } ?: text
    }

    fun parseColor(hex: String, defaultColor: Int = Color.BLACK): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            defaultColor
        }
    }

    fun showKeyboard(v: View) {
        showKeyboard(v, 0)
    }

    fun showKeyboard(v: View, delay: Long) {
        if (delay > 0) {
            val viewRef = WeakReference<View>(v)
            v.postDelayed({
                val view = viewRef.get()
                if (view != null) {
                    showKeyboard(view, 0)
                }
            }, delay)

            return
        }

        //show
        v.requestFocus()
        (v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?)
                ?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showKeyboard(c: AComponent<*, *, *>, delay: Long = 0) {
        showKeyboard(c.mView, delay)
    }

    @JvmStatic
    fun setSelectionEnd(view: EditText) {
        view.text?.takeIf { it.isNotEmpty() }?.let {
            try {
                view.setSelection(it.length)
            } catch (e: IndexOutOfBoundsException) {} //may happen, let's not count on Android
        }
    }

    @JvmStatic
    @JvmOverloads
    fun setTextWithSelectionEnd(view: EditText, text: CharSequence?, openKeyboard: Boolean = true) {
        view.setText(text)
        view.post {
            setSelectionEnd(view)
        }

        if (openKeyboard) {
            showKeyboard(view)
        }
    }
}}














fun StaticLayout.getLastVisibleLine(forViewHeightPx: Int): Int {
    return getLineForVertical(forViewHeightPx)
}

fun StaticLayout.getLineCharCount(line: Int): Int {
    return getLineVisibleEnd(line) - getLineStart(line)
}

fun StaticLayout.getVisibleCharCount(forViewHeightPx: Int): Long {

    return getLastVisibleLine(forViewHeightPx).let { lastVisibleLine ->

        //count all the visible chars
        (0..lastVisibleLine).toList()
            .map { getLineCharCount(it).toLong() }//THINK visibleEnd
            .sum()
            .let { min(it, text.size().toLong()) }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun StaticLayout.getEllipsizedText(view: TextView, forViewHeightPx: Int, endText: CharSequence?): CharSequence? {
    return if (text.isNullOrBlank()) { return text } else {
        getVisibleCharCount(forViewHeightPx)
        //replace last chars with endText
        .let { visibleCount ->
            if (endText.isNullOrBlank()) {
                text.take(visibleCount.toInt())

            } else {
                (visibleCount - endText.size()).toInt().let { endTextStartIndex ->

                    TextUtils.concat(
                        text.take(endTextStartIndex)
                        , endText
                    )
                }

            }
        }
    }
}

fun CharSequence?.size(): Int {
    return this?.length ?: 0
}