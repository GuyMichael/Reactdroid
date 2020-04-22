package com.guymichael.reactdroid.extensions.components.list.layouts.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.ComponentListUtils
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import kotlin.math.*

class SnappingRecyclerView : RecyclerView {
    var isSnappingEnabled = true
        private set
    private var isSnapOneOnFling = false
    private var deceleration: Double = 0.toDouble()

    /**
     * This implementation obviously doesn't take into account the direction of the
     * that preceded it, but there is no easy way to get that information without more
     * hacking than I was willing to put into it.
     */
    // Scrolled first view more than halfway offscreen
    //NOTICE: returns 0 if this.layoutManager is null
    private fun getFixScrollPos(): Int {
        return layoutManager?.let { layoutManager ->
            var childPos = 0
            val adapter = getCustomAdapter()

            if (this.childCount > 0 && adapter != null) {

                val child = getChildAt(0)
                childPos = ComponentListUtils.getFirstVisiblePosition(layoutManager)
                val lm = layoutManager as LinearLayoutManager

                if (lm.orientation == RecyclerView.HORIZONTAL && abs(child.left) > child.measuredWidth / 2) {
                    return childPos + 1
                } else if (lm.orientation == RecyclerView.VERTICAL && abs(child.top) > child.measuredWidth / 2) {
                    return childPos + 1
                }
            }

             adapter?.getActualPosition(childPos) ?: childPos

        } ?: 0
    }

    fun getCustomAdapter(): RecyclerComponentAdapter? {
        return adapter as? RecyclerComponentAdapter?
    }

    constructor(context: Context) : super(context) {
        calculateDeceleration(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        calculateDeceleration(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        calculateDeceleration(context)
    }

    /**
     * [LayoutManager] must be an instance of [LinearLayoutManager] to enable
     * @param enabled True by default (in case the given [LayoutManager] is a [LinearLayoutManager]).
     * @param snapOneAtATime False by default.
     */
    fun setSnappingEnabled(enabled: Boolean, snapOneAtATime: Boolean) {
        if (enabled) {
            val lm = layoutManager

            if (lm !is LinearLayoutManager) {
                isSnappingEnabled = false
                return
            }
            this.isSnapOneOnFling = snapOneAtATime
        }

        isSnappingEnabled = enabled
    }

    override fun setLayoutManager(layout: RecyclerView.LayoutManager?) {
        super.setLayoutManager(layout)

        if (layout !is LinearLayoutManager) {
            isSnappingEnabled = false
        }
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        if (isSnappingEnabled) {
            smoothScrollToPosition(getPositionForVelocity(velocityX, velocityY))
            return true
        }

        return super.fling(velocityX, velocityY)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        // We want the parent to handle all touch events--there's a lot going on there,
        // and there is no reason to overwrite that functionality--bad things will happen.
        val ret = super.onTouchEvent(e)

        if (isSnappingEnabled) {
            if ((e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) && scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                // The layout manager is a SnappyLayoutManager, which means that the
                // children should be snapped to a grid at the end of a drag or
                // fling. The motion event is either a user lifting their finger or
                // the cancellation of a motion events, so this is the time to take
                // over the scrolling to perform our own functionality.
                // Finally, the scroll state is idle--meaning that the resultant
                // velocity after the user's gesture was below the threshold, and
                // no fling was performed, so the view may be in an unaligned state
                // and will not be flung to a proper state.
                smoothScrollToPosition(getFixScrollPos())
            }
        }

        return ret
    }

    private fun getPositionForVelocity(velocityX: Int, velocityY: Int): Int {
        val lm = layoutManager as LinearLayoutManager

        if (childCount == 0) {
            return 0
        }

        return if (lm.orientation == RecyclerView.HORIZONTAL) {
            calcPosForVelocity(
                    velocityX, getChildAt(0).left, getChildAt(0).width, getCustomAdapter()?.getActualFirstVisiblePosition() ?: 0)
        } else {
            calcPosForVelocity(
                    velocityY, getChildAt(0).top, getChildAt(0).height, getCustomAdapter()?.getActualFirstVisiblePosition() ?: 0)
        }
    }

    private fun getVelocityConsiderLayoutDirection(rawVelocity: Int): Int {
        return if (layoutDirection == LayoutDirection.RTL)
            -rawVelocity
        else rawVelocity
    }

    private fun calcPosForVelocity(rawVelocity: Int, scrollPos: Int, childSize: Int, currPos: Int): Int {
        val velocity = getVelocityConsiderLayoutDirection(rawVelocity)

        if (isSnapOneOnFling) {
            return if (velocity < 0) {
                // Not sure if I need to lower bound this here.
                //return (int) Math.max(currPos + tempScroll / childSize, 0);
                max(currPos, 0)
            } else {
                //return (int) (currPos + (tempScroll / childSize) + 1);
                currPos + 1
            }

        } else {
            val v = sqrt((velocity * velocity).toDouble())
            val dist = getSplineFlingDistance(v)

            val tempScroll = scrollPos + if (velocity > 0) dist else -dist

            return if (velocity < 0) { //THINK check layoutDeirection here as well
                // Not sure if I need to lower bound this here.
                max(currPos + tempScroll / childSize, 0.0).toInt()
            } else {
                (currPos.toDouble() + tempScroll / childSize + 1.0).toInt()
            }
        }
    }

    private fun calculateDeceleration(context: Context) {
        deceleration = (SensorManager.GRAVITY_EARTH.toDouble() // g (m/s^2)

                * 39.3700787 // inches per meter

                // pixels per inch. 160 is the "default" dpi, i.e. one dip is one pixel on a 160 dpi
                // screen
                * context.resources.displayMetrics.density.toDouble() * 160.0 * FRICTION)
    }

    fun setDecelerationRate(rate: Float) {
        DECELERATION_RATE = rate
    }

    private fun getSplineFlingDistance(velocity: Double): Double {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return (ViewConfiguration.getScrollFriction().toDouble() * deceleration
                * exp(DECELERATION_RATE / decelMinusOne * l))
    }

    private fun getSplineDeceleration(velocity: Double): Double {
        return ln(INFLEXION * abs(velocity) / (ViewConfiguration.getScrollFriction() * deceleration))
    }

    companion object {
        // These variables are from android.widget.Scroller, which is used, via ScrollerCompat, by
        // Recycler View. The scrolling distance calculation logic originates from the same place. Want
        // to use their variables so as to approximate the look of normal Android scrolling.
        // Find the Scroller fling implementation in android.widget.Scroller.fling().
        private val INFLEXION = 0.35f // Tension lines cross at (INFLEXION, 1)
        private var DECELERATION_RATE = (ln(0.78) / ln(0.9)).toFloat()
        private val FRICTION = 0.84
    }
}