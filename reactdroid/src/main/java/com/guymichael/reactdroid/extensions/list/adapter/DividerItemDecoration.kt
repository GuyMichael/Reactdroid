package com.guymichael.reactdroid.extensions.list.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import android.view.View
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.Utils
import java.util.*

class DividerItemDecoration @JvmOverloads constructor(context: Context, protected var mOrientation: DECOR_ORIENTATION, divider: Drawable? = null) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    private var bottomDividerEnabled = true
    private var topDividerEnabled = false

    protected val mDivider: Drawable?

    private var cachedGridTopRowItemCount = -1
    private var cachedGridBottomRowItemCount = -1
    private var gridItemCountCacheDataObserver: SimpleAdapterDataObserver? = null

    enum class DECOR_ORIENTATION private constructor(internal var orientationHelperInt: Int) {
        VERTICAL_LIST(androidx.recyclerview.widget.LinearLayoutManager.VERTICAL), HORIZONTAL_LIST(androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL), GRID_VERTICAL_LIST(androidx.recyclerview.widget.GridLayoutManager.VERTICAL), GRID_HORIZONTAL_LIST(androidx.recyclerview.widget.GridLayoutManager.HORIZONTAL)
    }

    constructor(context: Context, orientation: DECOR_ORIENTATION, @DrawableRes divider: Int) : this(context, orientation, Utils.getDrawable(context, divider)) {}

    init {
        var dividerDrawable = divider
        if (dividerDrawable == null) {
            val a = context.obtainStyledAttributes(ATTRS)
            dividerDrawable = a.getDrawable(0)
            a.recycle()
        }

        mDivider = dividerDrawable
    }

    /**
     * Enables/disables bottom([DECOR_ORIENTATION.VERTICAL_LIST]) or right ([DECOR_ORIENTATION.HORIZONTAL_LIST]) divider
     * Default is [true][.bottomDividerEnabled]
     * @param enabled
     */
    fun setBottomDividerEnabled(enabled: Boolean): DividerItemDecoration {
        this.bottomDividerEnabled = enabled

        return this
    }

    fun isBottomDividerEnabled(): Boolean {
        return bottomDividerEnabled
    }

    /**
     * Enables/disables top([DECOR_ORIENTATION.VERTICAL_LIST]) or right ([DECOR_ORIENTATION.HORIZONTAL_LIST]) divider
     * Default is [false][.topDividerEnabled]
     * @param enabled
     */
    fun setTopDividerEnabled(enabled: Boolean): DividerItemDecoration {
        this.topDividerEnabled = enabled

        return this
    }

    fun isTopDividerEnabled(): Boolean {
        return topDividerEnabled
    }

    /**
     * Draws dividers on canvas
     */
    override fun onDraw(c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        //super.onDraw(c, parent, state); //calls the deprecated one
        drawDividers(c, parent, mOrientation)
    }

    /**
     * Applies list's items offsets, according to divider per index/childView
     */
    override fun getItemOffsets(outRect: Rect, child: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val dividerAfter = getDividerDrawableAfter(child, parent)
        val dividerBeforeIfFirstItem = if (isTopLeftItem(child, parent.getChildAdapterPosition(child), parent))
            getDividerDrawableBefore(child, parent)
        else
            null

        if (dividerAfter == null) {
            //no offset
            outRect.set(0, 0, 0, 0)
        } else {
            var offsetTop = 0
            var offsetLeft = 0
            var offsetBottom = 0
            var offsetRight = 0

            when (mOrientation) {
                DECOR_ORIENTATION.VERTICAL_LIST -> {
                    offsetBottom = dividerAfter.intrinsicHeight
                    if (dividerBeforeIfFirstItem != null) {
                        offsetTop = dividerBeforeIfFirstItem.intrinsicHeight
                    }
                }
                DECOR_ORIENTATION.HORIZONTAL_LIST -> {
                    offsetRight = dividerAfter.intrinsicWidth
                    if (dividerBeforeIfFirstItem != null) {
                        offsetLeft = dividerBeforeIfFirstItem.intrinsicWidth
                    }
                }
                DECOR_ORIENTATION.GRID_VERTICAL_LIST, DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                    offsetBottom = dividerAfter.intrinsicHeight
                    offsetRight = dividerAfter.intrinsicWidth

                    if (dividerBeforeIfFirstItem != null) {
                        offsetTop = dividerBeforeIfFirstItem.intrinsicHeight
                        offsetLeft = dividerBeforeIfFirstItem.intrinsicWidth
                    }
                }
            }

            //apply offsets
            outRect.set(offsetLeft, offsetTop, offsetRight, offsetBottom)
        }
    }

    /**
     * This method is called only if divider before should be drawn (e.g. [.isTopDividerEnabled]
     * returns `true` and `itemIndex == 0`).
     * <br></br>Extending class should override both this method and [.getDividerDrawableAfterImpl]
     * @return The drawable to draw **above/toLeftOf** the item with index 'itemIndex'
     */
    protected fun getDividerDrawableBeforeImpl(child: View, realIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return mDivider
    }

    /**
     * This method is called only if divider before should be drawn (e.g. [.isBottomDividerEnabled]
     * returns `true` and `itemIndex` is last in `parent`).
     * <br></br>Extending class should override both this method and [.getDividerDrawableBeforeImpl]
     * @param child
     * @param parent
     * @return The drawable to draw **below/toRightOf** the item with index 'itemIndex'
     */
    protected fun getDividerDrawableAfterImpl(child: View, realIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return mDivider
    }

    protected fun isTopLeftItem(child: View, itemIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Boolean {
        when (mOrientation) {
            DECOR_ORIENTATION.VERTICAL_LIST, DECOR_ORIENTATION.HORIZONTAL_LIST -> return itemIndex == 0
            DECOR_ORIENTATION.GRID_VERTICAL_LIST, DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                val columnCount = getTopRowItemCount(parent)
                return itemIndex < columnCount
            }
        }

        return false
    }

    protected fun isBottomRightItem(child: View, itemIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Boolean {
        val totalItemCount = parent.adapter?.itemCount ?: 0

        return when (mOrientation) {
            DECOR_ORIENTATION.VERTICAL_LIST, DECOR_ORIENTATION.HORIZONTAL_LIST -> itemIndex == totalItemCount - 1
            DECOR_ORIENTATION.GRID_VERTICAL_LIST, DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                val columnCount = getBottomRowItemCount(parent)
                itemIndex > totalItemCount - 1 - columnCount
            }
            else -> false
        }
    }

    private fun getDividerDrawableBefore(child: View, realIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return if (this.topDividerEnabled || !isTopLeftItem(child, realIndex, parent)//shouldDrawDividerBefore
        )
            getDividerDrawableBeforeImpl(child, realIndex, parent)
        else
            null//don't draw before first child / row;
    }

    private fun getDividerDrawableAfter(child: View, realIndex: Int, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return if (this.bottomDividerEnabled || !isBottomRightItem(child, realIndex, parent)//shouldDrawDividerAfter
        )
            getDividerDrawableAfterImpl(child, realIndex, parent)
        else
            null//last bottom/right divider disabled and last child / row
    }

    private fun calcVerticalDividerBoundsBelow(child: View, divider: Drawable, parentBounds: Rect): Rect {
        //draw below child
        val params = child.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        val top = child.bottom + params.bottomMargin
        val bottom = top + divider.intrinsicHeight

        return Rect(parentBounds.left, top, parentBounds.right, bottom)
    }

    private fun calcHorizontalDividerBoundsToRightOf(child: View, divider: Drawable, parentBounds: Rect): Rect {
        //draw to right of child
        val params = child.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        val left = child.right + params.rightMargin
        val right = left + divider.intrinsicWidth

        return Rect(left, parentBounds.top, right, parentBounds.bottom)
    }

    private fun calcVerticalDividerBoundsAbove(child: View, divider: Drawable, parentBounds: Rect): Rect {
        //draw below child
        val params = child.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        val bottom = child.top - params.topMargin//maybe topMargin should be ignored as it's possibly counted with getTop
        val top = bottom - divider.intrinsicHeight

        return Rect(parentBounds.left, top, parentBounds.right, bottom)
    }

    private fun calcHorizontalDividerBoundsToLeftOf(child: View, divider: Drawable, parentBounds: Rect): Rect {
        //draw to left of child
        val params = child.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        val right = child.left - params.leftMargin//maybe leftMargin should be ignored as it's possibly counted with getLeft
        val left = right - divider.intrinsicWidth

        return Rect(left, parentBounds.top, right, parentBounds.bottom)
    }

    /**
     * Convenience method. Calls [.getDividerDrawableBefore]
     * with `realIndex = parent.getChildAdapterPosition(child)`
     */
    private fun getDividerDrawableBefore(child: View, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return getDividerDrawableBefore(child, parent.getChildAdapterPosition(child), parent)
    }

    /**
     * Convenience method. Calls [.getDividerDrawableAfter]
     * with `realIndex = parent.getChildAdapterPosition(child)`
     */
    private fun getDividerDrawableAfter(child: View, parent: androidx.recyclerview.widget.RecyclerView): Drawable? {
        return getDividerDrawableAfter(child, parent.getChildAdapterPosition(child), parent)
    }

    private fun drawChildDividers(child: View, realIndex: Int, c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, orientation: DECOR_ORIENTATION) {
        val itemCount = parent.adapter?.itemCount ?: 0
        val parentBounds = getParentBounds(parent)

        //draw before child
        val dividerBefore = getDividerDrawableBefore(child, realIndex, parent)
        if (dividerBefore != null) {
            for (bounds in calcChildDividerBoundsBefore(child, realIndex, itemCount, dividerBefore, parentBounds, orientation)) {
                dividerBefore.bounds = bounds
                dividerBefore.draw(c)
                //TODO draw (color) not working
            }
        }

        //draw after child
        val dividerAfter = getDividerDrawableAfter(child, realIndex, parent)
        if (dividerAfter != null) {
            for (bounds in calcChildDividerBoundsAfter(child, realIndex, itemCount, dividerAfter, parentBounds, orientation)) {
                dividerAfter.bounds = bounds
                dividerAfter.draw(c)
                //TODO draw (color) not working
            }
        }
    }

    private fun drawDividers(c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, orientation: DECOR_ORIENTATION) {
        val childCount = parent.childCount

        val firstVisible = (if (parent.childCount > 0) parent.getChildAt(0) else null) ?: return

        val firstVisibleRealPosition = parent.getChildAdapterPosition(firstVisible)

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val realIndex = i + firstVisibleRealPosition

            drawChildDividers(child, realIndex, c, parent, orientation)
        }
    }

    private fun calcChildDividerBoundsBefore(child: View, realIndex: Int, itemCount: Int, divider: Drawable, parentBounds: Rect, orientation: DECOR_ORIENTATION): List<Rect> {

        val bounds = ArrayList<Rect>()
        when (orientation) {
            DECOR_ORIENTATION.VERTICAL_LIST -> bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))
            DECOR_ORIENTATION.HORIZONTAL_LIST -> bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))
            DECOR_ORIENTATION.GRID_VERTICAL_LIST -> {
                bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))
                bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))
            }
            DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))
                bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))
            }
            else -> {
            }
        }

        return bounds
    }

    private fun calcChildDividerBoundsAfter(child: View, realIndex: Int, itemCount: Int, divider: Drawable, parentBounds: Rect, orientation: DECOR_ORIENTATION): List<Rect> {

        val bounds = ArrayList<Rect>()
        when (orientation) {
            DECOR_ORIENTATION.VERTICAL_LIST -> bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))
            DECOR_ORIENTATION.HORIZONTAL_LIST -> bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))
            DECOR_ORIENTATION.GRID_VERTICAL_LIST -> {
                bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))
                bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))
            }
            DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))
                bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))
            }
            else -> {
            }
        }

        return bounds
    }

    private fun getParentBounds(parent: androidx.recyclerview.widget.RecyclerView): Rect {
        return Rect(
                parent.paddingLeft, parent.paddingTop, parent.width - parent.paddingRight, parent.height - parent.paddingBottom)
    }

    private fun getTopRowItemCount(parent: androidx.recyclerview.widget.RecyclerView): Int {
        if (cachedGridTopRowItemCount > -1) {
            return cachedGridTopRowItemCount
        }

        val totalItemCount = parent.adapter?.itemCount ?: 0
        var realColumnCount = 1//default 1

        if (totalItemCount > 1) {
            when (mOrientation) {
                DECOR_ORIENTATION.GRID_VERTICAL_LIST, DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                    val layoutManager = parent.layoutManager

                    if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
                        val spanCount = layoutManager.spanCount
                        val maxItemsInFirstRow = Math.min(spanCount, totalItemCount)
                        val spanSizeLookup = layoutManager.spanSizeLookup
                        realColumnCount = 0

                        //count items in first row/line
                        var i = 0
                        while (i < maxItemsInFirstRow && spanSizeLookup.getSpanGroupIndex(i, spanCount) == 0) {
                            realColumnCount++
                            i++
                        }
                    } else {
                        if (BuildConfig.DEBUG) {
                            Logger.w(DividerItemDecoration::class, "getTopRowItemCount(): unsupported Grid LayoutManager of class: " + layoutManager?.javaClass?.simpleName)
                        }
                    }
                }
                else -> {}
            }
        }

        //update cache
        cachedGridTopRowItemCount = realColumnCount
        //empty cache on data changes
        addGridItemCountCacheDataObserver(parent)

        return realColumnCount
    }

    private fun getBottomRowItemCount(parent: androidx.recyclerview.widget.RecyclerView): Int {
        if (cachedGridBottomRowItemCount > -1) {
            return cachedGridBottomRowItemCount
        }

        val totalItemCount = parent.adapter?.itemCount ?: 0
        var realColumnCount = 1//default 1

        if (totalItemCount > 1) {
            when (mOrientation) {
                DECOR_ORIENTATION.GRID_VERTICAL_LIST, DECOR_ORIENTATION.GRID_HORIZONTAL_LIST -> {
                    val layoutManager = parent.layoutManager
                    if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
//just get the index+1 of the last item, inside it's span group(of size spanCount())
                        realColumnCount = layoutManager.spanSizeLookup.getSpanIndex(totalItemCount - 1, layoutManager.spanCount) + 1
                    } else {
                        if (BuildConfig.DEBUG) {
                            Logger.w(DividerItemDecoration::class, "getBottomRowItemCount(): unsupported Grid LayoutManager of class: " + layoutManager?.javaClass?.simpleName)
                        }
                    }
                }
                else -> {}
            }
        }

        //update cache
        cachedGridBottomRowItemCount = realColumnCount
        //empty cache on data changes
        addGridItemCountCacheDataObserver(parent)

        return realColumnCount
    }

    private fun addGridItemCountCacheDataObserver(parent: androidx.recyclerview.widget.RecyclerView) {
        if (gridItemCountCacheDataObserver == null) {
            object : SimpleAdapterDataObserver() {
                override fun onChanged() {
                    cachedGridBottomRowItemCount = -1
                    cachedGridTopRowItemCount = -1
                }
            }.let {
                gridItemCountCacheDataObserver = it
                parent.adapter?.registerAdapterDataObserver(it)
            }
        }
    }

    private fun removeGridItemCountCacheDataObserver(parent: androidx.recyclerview.widget.RecyclerView) {
        gridItemCountCacheDataObserver?.let {
            parent.adapter?.unregisterAdapterDataObserver(it)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}