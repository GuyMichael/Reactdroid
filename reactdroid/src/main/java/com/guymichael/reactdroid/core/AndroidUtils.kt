package com.guymichael.reactdroid.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange

object AndroidUtils {

    @SuppressLint("NewApi")
    fun getDrawable(context: Context, @DrawableRes resId: Int, clone: Boolean): Drawable? {

        if (resId == 0) {
            return null
        }

        val d: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(resId)
        } else {
            context.resources.getDrawable(resId)
        }

        return try {
            if (d != null && clone) d.constantState!!.newDrawable().mutate() else d
        } catch (e: NullPointerException) {
            e.printStackTrace()
            d
        }
    }

    /**
     * Sets alpha component, but can preserve color's alpha, treating it as a RGBA color, in contrast with
     * Android's `ColorUtils.setAlphaComponent` which overrides the original alpha
     * @param color
     * @param alpha 0f - 1f
     * @param overrideBaseAlpha if true, the method will ignore color's original alpha, else, it will be multiplied with 'alpha'
     * @return new color int
     */
    @ColorInt
    fun setAlphaComponent(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float,
        overrideBaseAlpha: Boolean
    ): Int {

        require(!(alpha < 0 || alpha > 255)) { "alpha must be between 0 and 255." }

        val alphaInt = (alpha * if (overrideBaseAlpha) 255 else Color.alpha(color)).toInt()

        return color and 0x00ffffff or (alphaInt shl 24)
    }
}