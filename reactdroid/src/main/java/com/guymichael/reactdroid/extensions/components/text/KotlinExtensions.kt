package com.guymichael.reactdroid.extensions.components.text

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.guymichael.reactdroid.core.applyOrGone
import com.guymichael.reactdroid.core.applyOrInvisible
import com.guymichael.reactdroid.core.model.AComponent

fun <P : BaseTextProps> AComponent<P, *, *>.renderBaseTextOrGone(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrGone(props?.takeIf { !it.text.isNullOrBlank() }, {
        onRender(it)
        if (this.visibility != View.VISIBLE) {
            this.visibility = View.VISIBLE
        }
    }, *visibilityBoundViews)
}

fun <P : BaseTextProps> AComponent<P, *, *>.renderBaseTextOrInvisible(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrInvisible(props?.takeIf { !it.text.isNullOrBlank() }, {
        onRender(it)
        if (this.visibility != View.VISIBLE) {
            this.visibility = View.VISIBLE
        }
    }, *visibilityBoundViews)
}

/*fun AVComponent<BaseATextProps, *, *>.renderBaseTextOrGone(text: CharSequence?, vararg visibilityBoundViews: View) {
    renderBaseTextOrGone(text?.let(::ATextProps), *visibilityBoundViews) //will also check for empty
}

fun AVComponent<BaseATextProps, *, *>.renderBaseTextOrInvisible(text: CharSequence?, vararg visibilityBoundViews: View) {
    renderBaseTextOrInvisible(text?.let(::ATextProps), *visibilityBoundViews) //will also check for empty
}*/

/** Keeps/makes View visible no matter the text value */
fun <V : TextView> AComponent<TextProps, *, V>.renderText(text: CharSequence?) {
    onRender(TextProps(text))
    if (mView.visibility != View.VISIBLE) {
        mView.visibility = View.VISIBLE
    }
}

fun <V : TextView> AComponent<TextProps, *, V>.renderTextColor(@ColorInt color: Int) {
    if (mView.currentTextColor != color) {
        mView.setTextColor(color)
    }
}

fun <V : TextView> AComponent<TextProps, *, V>.renderTextColorRes(@ColorRes colorRes: Int) {
    ContextCompat.getColor(mView.context, colorRes).let(::renderTextColor)
}

/** Keeps/makes View visible no matter the text value
 * @param textRes if 0 (valid), will act like [renderText] with null */
fun <V : TextView> AComponent<TextProps, *, V>.renderTextRes(textRes: Int) {
    onRender(TextProps(
        textRes.takeIf { it != 0 }?.let {
            mView.context?.getText(it)
        }
    ))

    if (mView.visibility != View.VISIBLE) {
        mView.visibility = View.VISIBLE
    }
}

fun AComponent<TextProps, *, *>.renderTextOrGone(text: CharSequence?, vararg visibilityBoundViews: View) {
    renderBaseTextOrGone(text?.let(::TextProps), *visibilityBoundViews) //will also check for empty
}

fun AComponent<TextProps, *, *>.renderTextOrInvisible(text: CharSequence?, vararg visibilityBoundViews: View) {
    renderBaseTextOrInvisible(text?.let(::TextProps), *visibilityBoundViews) //will also check for empty
}

fun AComponent<CollapsingTextProps, *, *>.renderCTextOrGone(text: CharSequence?
                                                            , expandBtnText: CharSequence? = null, vararg visibilityBoundViews: View) {

    renderBaseTextOrGone(text?.run {
        if (expandBtnText == null) {
            CollapsingTextProps(this)
        } else {
            CollapsingTextProps(this, expandBtnText = expandBtnText)
        }
    }, *visibilityBoundViews) //will also check for empty
}

fun AComponent<CollapsingTextProps, *, *>.renderCTextOrInvisible(text: CharSequence?, vararg visibilityBoundViews: View) {
    renderBaseTextOrInvisible(text?.run { CollapsingTextProps(this) }, *visibilityBoundViews) //will also check for empty
}

fun AComponent<*, *, *>.renderEnabled(enabled: Boolean/*, clickableFocusable: Boolean? = enabled*/) {
    mView.isEnabled = enabled //setter already checks for isEnabled != enabled

    /*clickableFocusable?.let { //these setters has internal checks, but after some calculations
        if (it != mView.isClickable) {
            mView.isClickable = it
        }
        if (it != mView.isFocusable) {
            mView.isFocusable = it
        }
        if (it != mView.isFocusableInTouchMode) {
            mView.isFocusableInTouchMode = it
        }
    }*/
}