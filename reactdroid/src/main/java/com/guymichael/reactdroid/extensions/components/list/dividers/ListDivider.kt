package com.guymichael.reactdroid.extensions.components.list.dividers

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt

data class ListDivider(val widthPx: Int, val heightPx: Int, @ColorInt val argbColor: Int? = null) {

    constructor(sizePx: Int)
    : this(sizePx, sizePx)

    fun toDrawable(): Drawable {
        return GradientDrawable().also {
            it.setSize(widthPx, heightPx)
            argbColor?.also(it::setColor)
        }
    }
}