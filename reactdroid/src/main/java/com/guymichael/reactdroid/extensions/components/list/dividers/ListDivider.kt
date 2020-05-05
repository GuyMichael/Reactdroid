package com.guymichael.reactdroid.extensions.components.list.dividers

import androidx.annotation.ColorInt

data class ListDivider(val widthPx: Int, val heightPx: Int, @ColorInt val argbColor: Int? = null) {

    constructor(sizePx: Int)
    : this(sizePx, sizePx)

    constructor(sizePx: Int, @ColorInt argbColor: Int)
    : this(sizePx, sizePx, argbColor)

    /*fun toDrawable(): Drawable {
        return GradientDrawable().also {
            it.setSize(widthPx, heightPx)
            argbColor?.also(it::setColor)
        }
    }*/
}