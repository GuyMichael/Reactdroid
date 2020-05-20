package com.guymichael.reactdroid.extensions.animation

import android.animation.*
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.core.AndroidUtils
import com.guymichael.reactdroid.core.waitForMeasure

object AnimUtils {

    const val defaultVisibilityAnimDuration = 150L

    fun isSystemAnimationsEnabled(context: Context): Boolean {
        return getAnimationScale(context) != 0F
    }

    fun getAnimationScale(context: Context): Float {
        return Settings.Global.getFloat(context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)

        /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getFloat(context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
        } else {
            //noinspection deprecation
            Settings.System.getFloat(context.contentResolver,
                    Settings.System.ANIMATOR_DURATION_SCALE, 1.0f);
        }*/
    }


    /**
     * Animates from *view's* current background color to *colorTo*.<br></br>
     * **Note: **the `view`'s `Drawable` background must be a [ColorDrawable] (just a colorRes defined in xml for example),
     * or the method will use a transparent colorFrom instead.<br></br>
     * @param view
     * @param colorTo
     * @return the used (and started) [animator][ValueAnimator]
     */
    fun animateBackgroundColorChange(view: View, @ColorInt colorTo: Int, durationMs: Long)
    : ValueAnimator {
        @ColorInt val colorFrom: Int = try {
            (view.background as ColorDrawable).color
        } catch (e: ClassCastException) {
            e.printStackTrace()
            AndroidUtils.setAlphaComponent(colorTo, 0f, true) //transparent
        } catch (e: NullPointerException) {
            e.printStackTrace()
            AndroidUtils.setAlphaComponent(colorTo, 0f, true) //transparent
        }

        return animateColorChange(colorFrom, colorTo, durationMs) {
            view.setBackgroundColor(it)
        }
    }

    /**
     * @param colorFrom
     * @param colorTo
     * @param durationMs
     * @param updateColorConsumer for applying the color in whatever way you desire (e.g. [View.setBackgroundColor])
     * @return the used (and started) [animator][ValueAnimator]
     */
    fun animateColorChange(@ColorInt colorFrom: Int, @ColorInt colorTo: Int, durationMs: Long,
        updateColorConsumer: (Int) -> Unit
    ): ValueAnimator {
        return ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).also {
            it.duration = durationMs
            it.addUpdateListener { animator -> updateColorConsumer.invoke(animator.animatedValue as Int) }
            it.start()
        }
    }




    fun animateImageChange(view: ImageView, @DrawableRes toDrawable: Int, durationMs: Long): ObjectAnimator {
        return animateImageChange(view
            , toDrawable.takeIf { it != 0 }?.let {
                AndroidUtils.getDrawable(view.context, toDrawable, false)
            }
            , durationMs)
    }

    fun animateImageChange(view: ImageView, toDrawable: Drawable?, durationMs: Long): ObjectAnimator {
        return animateContentChange(view, durationMs, {
            it.setImageDrawable(toDrawable);
        })
    }




    fun animateTextChange(view: TextView, @StringRes toText: Int, durationMs: Long): ObjectAnimator {
        return animateTextChange(view
            , toText.takeIf { it != 0 }?.let { view.context.getText(toText) }
            , durationMs
        )
    }

    fun animateTextChange(textView: TextView, toText: CharSequence?, durationMs: Long): ObjectAnimator {
        return animateContentChange(textView, durationMs, {
            it.text = toText
        })
    }




    /**
     * Animates alpha: original -> 0 -> original, with *listener* being called on alpha 0 to change the requited content
     * @param obj
     * @param fadeOutDurationMs
     * @param changeContentConsumer this is where you apply your desired content change to T
     * @param fadeInDurationMs default is same as 'fadeOutDurationMs'
     * @param interpolator
     * @param <T>
     * @return the animator object created and used
    </T> */
    @JvmStatic
    @JvmOverloads
    fun <T : View> animateContentChange(obj: T, fadeOutDurationMs: Long, changeContentConsumer: (T) -> Unit
        , fadeInDurationMs: Long = fadeOutDurationMs
        , interpolator: Interpolator = AccelerateInterpolator()
    ): ObjectAnimator {

        return animateFloat(obj, "alpha", 0f, fadeOutDurationMs
            , 0
            , interpolator

        ).apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    animation.removeListener(this)

                    //change content
                    changeContentConsumer(obj)

                    //resume original alpha
                    //reverse the alpha animation, at end of execution queue, to let the content change
                    Handler().post {
                        duration = fadeInDurationMs
                        reverse()
                    }
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }





    fun animateRotation(rotateDrawable: Drawable, duration: Long
        , startDelay: Long = 0
        , startNow: Boolean = true
        , repeatCount: Int = ObjectAnimator.INFINITE
        , reversed: Boolean = false
    ): ObjectAnimator {
        return animateLevel(rotateDrawable, duration, startDelay, startNow, repeatCount, reversed)
    }

    fun animateRotation(view: ProgressBar, duration: Long
        , startNow: Boolean = true
        , interpolator: TimeInterpolator = LinearInterpolator()
        , repeatCount: Int = ObjectAnimator.INFINITE
    ): ObjectAnimator {

        return ObjectAnimator.ofInt(view, "progress", 0, 10000).also {

            it.interpolator = interpolator
            it.repeatCount = repeatCount
            it.duration = duration
            if (startNow) { it.start() }
        }
    }

    fun animateProgress(view: ProgressBar, fromProgress: Int, toProgress: Int, duration: Long
        , delay: Long = 0
        , interpolator: TimeInterpolator = LinearInterpolator()
        , startNow: Boolean = true
    ): ObjectAnimator {

        return ObjectAnimator.ofInt(view, "progress", fromProgress, toProgress).also {

            it.interpolator = interpolator
            it.repeatCount = 0
            it.duration = duration
            it.startDelay = delay
            if (startNow) { it.start() }
        }
    }






    /**
     * Starting a rotation animation on the given drawable.
     * @param drawable
     * @param duration
     * @param startDelay
     * @param startNow whether to call [ObjectAnimator.start]
     * @return The new [ObjectAnimator]
     */
    fun animateLevel(drawable: Drawable, duration: Long
         , startDelay: Long = 0
         , startNow: Boolean = true
         , repeatCount: Int = ObjectAnimator.INFINITE
         , reversed: Boolean = false
    ): ObjectAnimator {

        return ObjectAnimator.ofInt(drawable,
            "level",
            if (reversed) 10000 else 0,
            if (reversed) 0 else 10000

        ).also {
            it.interpolator = LinearInterpolator()
            it.repeatCount = repeatCount
            it.duration = duration
            it.startDelay = startDelay
            if (startNow) { it.start() }
        }
    }





    /** animates alpha to 1 and sets the target visibility to VISIBLE, skips if already visible and alpha is already 1
     * @return promise, cancelled when/if the view is destroyed */
    fun <V : View> animateFadeIn(target: V
            , duration: Long
            , startAlpha: Float = if (target.visibility == View.VISIBLE) target.alpha else 0F
            , startDelay: Long = 0
            , interpolator: Interpolator = LinearInterpolator()
        ): APromise<V> {

        if ((target.visibility == View.VISIBLE && target.alpha == 1F)
                || duration == 0L
                || startAlpha == 1F) {
            //already shown or 1 desired alpha / 0 duration. Make sure alpha is 1 as this method suggests
            return APromise.ofView(target, true)
                .then {
                    //'fillAfter'
                    it.alpha = 1f
                    it.visibility = View.VISIBLE
                }
        }

        return APromise.ofView(target, true) //delay with promise, so that the starting values will also update delayed
            .thenAwait{
                //'fillBefore'
                it.alpha = startAlpha
                it.visibility = View.VISIBLE //we must start visible for animation (alpha) to show

                promiseOfAnimator(AnimUtils.animateFloat(
                    it, "alpha", 1f, duration, startDelay, interpolator, false)
                ).thenViewOrCancel(it)
            }
    }

    /** animates alpha to 0. Note: does NOT change visibility!
     * @return promise, cancelled when/if the view is destroyed */
    fun <V : View> animateFadeOut(target: V, duration: Long = 150
            , startAlpha: Float = target.alpha, startDelay: Long = 0
            , startVisibility: Int = target.visibility
            , interpolator: Interpolator = LinearInterpolator()
        ): APromise<V> {

        //THINK if the same animation is currently running, the following will cancel it for nothing
        if (startVisibility != View.VISIBLE || duration == 0L || startAlpha == 0F) {
            //already hidden or 0 desired alpha / duration. Make sure alpha is 0 as this method suggests
            return APromise.ofView(target, true)
                .then {
                    //'fillAfter'
                    it.alpha = 0F
                }
        }

        return APromise.ofView(target, true) //delay with promise, so that the starting values will also update delayed
            .thenAwait {
                //'fillBefore'
                it.alpha = startAlpha
                it.visibility = startVisibility

                promiseOfAnimator(animateFloat(
                    it, "alpha", 0f, duration, startDelay, interpolator, false
                )).thenViewOrCancel(it)
            }
    }

    /** animates alpha to 0 and sets the target visibility to GONE
     * @return promise, cancelled when/if the view is destroyed */
    fun <T : View> animateFadeOutAndGone(target: T, duration: Long = 150
        , startAlpha: Float = target.alpha
        , startDelay: Long = 0
        , startVisibility: Int = target.visibility
        , interpolator: Interpolator = LinearInterpolator()
    ): APromise<T> {
        return animateFadeOutAndVisibility(target, View.GONE, duration, startAlpha, startDelay, startVisibility, interpolator)
    }

    /** animates alpha to 0 and sets the target visibility to INVISIBLE
     * @return promise, cancelled when/if the view is destroyed */
    fun <T : View> animateFadeOutAndInvisible(target: T, duration: Long = 150
            , startAlpha: Float = target.alpha
            , startDelay: Long = 0
            , startVisibility: Int = target.visibility
            , interpolator: Interpolator = LinearInterpolator()): APromise<T> {
        return animateFadeOutAndVisibility(target, View.INVISIBLE, duration, startAlpha, startDelay, startVisibility, interpolator)
    }

    fun <T : View> animateFadeAndSlideIn(view: T, startOffset: (T) -> Pair<Float, Float>
        , duration: Long = 150
        , startDelay: Long = 0
        , interpolator: Interpolator = LinearInterpolator()
    ) {

        view.waitForMeasure().then { v ->
            //'fillBefore'
            startOffset(v).apply {
                v.translationX = first
                v.translationY = second
            }
            v.alpha = 0F

            AnimatorSet().apply {
                playTogether(
                    animateTranslationY(v, 0F, startDelay, duration, interpolator, true)
                    , animateFloat(v, "alpha", 1f, duration, startDelay, interpolator, true)
                )
            }

        }.execute()
    }

    fun <T : View> animateFadeAndSlideInFromBottom(target: T
            , startOffset: (T) -> Pair<Float, Float> = { Pair(0F, it.measuredHeight.toFloat()) }
            , duration: Long = 150
            , startDelay: Long = 0
            , interpolator: Interpolator = LinearInterpolator()
        ) {

        animateFadeAndSlideIn(target, startOffset, duration, startDelay, interpolator)
    }






    /** animates alpha to 1 and sets the target visibility to VISIBLE
     * @return promise, cancelled when/if the view is destroyed */
    fun <T : View> animateTranslationY(target: T
            , toValue: Float
            , duration: Long
            , interpolator: Interpolator = DecelerateInterpolator()
            , fromValue: Float = target.translationY
            , startDelay: Long = 0): APromise<T> {

        if (target.translationY == toValue) {
            //already there
            return APromise.ofView(target, true)
        }

        return APromise.ofView(target, true)
            .thenAwait {
                //'fillBefore'
                it.translationY = fromValue

                promiseOfAnimator(animateTranslationY(
                    it, toValue, startDelay, duration, interpolator, false)
                )
                .thenViewOrCancel(it)
            }
    }

    fun animateTranslationY(view: View, value: Float, duration: Long
        , startDelay: Long = 0
        , interpolator: TimeInterpolator = LinearInterpolator()
        , startNow: Boolean = true
    ): ObjectAnimator {
        return animateFloat(view, "translationY",
            value, duration, startDelay, interpolator, startNow
        )
    }







    /** @see also ViewAnimationUtils.createCircularReveal */
    fun createLinearResize(viewToReveal: View, startWidth: Int, startHeight: Int
            , targetWidth: Int, targetHeight: Int
            , duration: Long
            , startDelay: Long = 0
            , interpolator: Interpolator = AccelerateDecelerateInterpolator()): AnimatorSet {
        viewToReveal.clearAnimation()

        viewToReveal.layoutParams.height = startHeight
        viewToReveal.requestLayout()

        val heightAnimator = viewToReveal.takeIf { startHeight != targetHeight }?.let {
            ValueAnimator.ofInt(startHeight, targetHeight).apply {
                addUpdateListener { animation -> //THINK weak ref?
                    it.layoutParams.height = animation.animatedValue as Int
                    it.requestLayout()
                }
            }
        }

        val widthAnimator = viewToReveal.takeIf { startWidth != targetWidth }?.let {
            ValueAnimator.ofInt(startWidth, targetWidth).apply {
                addUpdateListener { animation ->
                    viewToReveal.layoutParams.width = animation.animatedValue as Int
                    viewToReveal.requestLayout()
                }
            }
        }

        return AnimatorSet().apply {
            this.duration = duration
            this.startDelay = startDelay
            this.interpolator = interpolator
            playTogether(listOfNotNull(widthAnimator, heightAnimator))
        }
    }

    fun createLinearReveal(viewToReveal: View, startWidth: Int, startHeight: Int, duration: Long
           , startDelay: Long = 0
           , interpolator: Interpolator = AccelerateDecelerateInterpolator()): AnimatorSet {

        return createLinearResize(viewToReveal, startWidth, startHeight
                , viewToReveal.measuredWidth, viewToReveal.measuredHeight
                , duration, startDelay, interpolator)
    }

















    fun animateFloat(view: View, propertyName: String, value: Float, duration: Long
        , startDelay: Long = 0
        , interpolator: TimeInterpolator = LinearInterpolator()
        , startNow: Boolean = true
    ): ObjectAnimator {

        return animate(
            ObjectAnimator.ofFloat(view, propertyName, value),
            duration,
            startDelay,
            interpolator,
            startNow
        )
    }

    fun animateInt(view: View, propertyName: String, value: Int, duration: Long
        , startDelay: Long = 0
        , interpolator: TimeInterpolator = LinearInterpolator()
        , startNow: Boolean = true
    ): ObjectAnimator {
        return animate(
            ObjectAnimator.ofInt(view, propertyName, value)
            , duration, startDelay, interpolator, startNow
        )
    }






    fun animate(context: Context, @AnimRes animResId: Int, v: View, startDelay: Long = 0): Animation {
        val anim = AnimationUtils.loadAnimation(context, animResId)

        if (startDelay > 0) {
            anim.startOffset = startDelay
        }

        anim.reset()
        v.clearAnimation()
        v.startAnimation(anim)

        return anim
    }
}














/** @param animator not started! */
private fun promiseOfAnimator(animator: ObjectAnimator): APromise<Unit> {
    return APromise.ofCallback<Unit> { promiseCallback ->
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {
                //THINK sometimes called right away(!) understand why
                promiseCallback.onCancel("Animation cancelled")
            }
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                promiseCallback.onSuccess(Unit)
            }
        })

        animator.start()
    }
}

private fun <V : View> animateFadeOutAndVisibility(target: V, targetVisibility: Int, duration: Long
        , startAlpha: Float = target.alpha, startDelay: Long = 0
        , startVisibility: Int = target.visibility
        , interpolator: Interpolator = LinearInterpolator()): APromise<V> {

    if (startVisibility != View.VISIBLE || duration == 0L || startAlpha == 0F) {
        //already hidden or 0 alpha / duration. Make sure alpha is 0 as this method suggests
        return APromise.ofView(target, true).then {
            //'fillAfter'
            it.visibility = targetVisibility
            it.alpha = 0F // --> in place of animateFadeOut()
        }
    }

    return AnimUtils.animateFadeOut(target, duration, startAlpha, startDelay, startVisibility, interpolator)
        .then {
            //'fillAfter'
            it.visibility = targetVisibility
        }
}

private fun animate(
    animator: ObjectAnimator,
    duration: Long,
    startDelay: Long,
    interpolator: TimeInterpolator,
    startNow: Boolean
): ObjectAnimator {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        animator.setAutoCancel(true)
    }

    animator.startDelay = startDelay
    animator.duration = duration
    animator.interpolator = interpolator
    if (startNow) { animator.start() }

    return animator
}