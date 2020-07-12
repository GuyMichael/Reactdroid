package com.guymichael.reactdroid.extensions.components.list.dividers

import androidx.annotation.ColorInt

data class ListDivider @JvmOverloads constructor(
    val widthPx: Int
    , val heightPx: Int = widthPx
    , @ColorInt val argbColor: Int? = null
)/* {

    fun toDrawable(): Drawable {
        return GradientDrawable().also {
            it.setSize(widthPx, heightPx)
            argbColor?.also(it::setColor)
        }
    }
}*/