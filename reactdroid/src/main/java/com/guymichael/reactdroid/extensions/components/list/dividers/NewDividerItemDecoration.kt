package com.guymichael.reactdroid.extensions.components.list.dividers

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.extensions.components.list.adapter.model.SimpleAdapterDataObserver

open class NewDividerItemDecoration (
        protected val mOrientation: @ListDividerOrientation Int
        , protected val mDivider: ListDivider
        , private val mTopDivider: ListDivider? = mDivider
        , private val mBottomDivider: ListDivider? = mTopDivider
    ) : RecyclerView.ItemDecoration() {

    private var cachedGridTopRowItemCount = -1
    private var cachedGridBottomRowItemCount = -1
    private var gridItemCountCacheDataObserver: SimpleAdapterDataObserver? = null



    constructor(orientation: @ListDividerOrientation kotlin.Int
        , width: Int, height: Int = width
    ): this(orientation, ListDivider(width, height))

    /**
     * Draws dividers on canvas
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        //super.onDraw(c, parent, state); //calls the deprecated one
        drawDividers(c, parent, mOrientation)
    }

    /**
     * Applies list's items offsets, according to divider per index/childView
     */
    override fun getItemOffsets(outRect: Rect, child: View, parent: RecyclerView, state: RecyclerView.State) {
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
                ListDividerOrientation.VERTICAL -> {
                    offsetBottom = dividerAfter.heightPx
                    if (dividerBeforeIfFirstItem != null) {
                        offsetTop = dividerBeforeIfFirstItem.heightPx
                    }
                }
                ListDividerOrientation.HORIZONTAL -> {
                    offsetRight = dividerAfter.widthPx
                    if (dividerBeforeIfFirstItem != null) {
                        offsetLeft = dividerBeforeIfFirstItem.widthPx
                    }
                }
                ListDividerOrientation.GRID_VERTICAL, ListDividerOrientation.GRID_HORIZONTAL -> {
                    offsetBottom = dividerAfter.heightPx
                    offsetRight = dividerAfter.widthPx

                    if (dividerBeforeIfFirstItem != null) {
                        offsetTop = dividerBeforeIfFirstItem.heightPx
                        offsetLeft = dividerBeforeIfFirstItem.widthPx
                    }
                }
            }

            //apply offsets
            outRect.set(offsetLeft, offsetTop, offsetRight, offsetBottom)
        }
    }

    protected open fun getTopDivider(child: View, parent: RecyclerView): ListDivider? {
        return mTopDivider
    }

    protected open fun getBottomDivider(child: View, parent: RecyclerView): ListDivider? {
        return mBottomDivider
    }

    /**
     * This method is called only if divider before should be drawn (e.g. [.isTopDividerEnabled]
     * returns `true` and `itemIndex == 0`).
     * <br></br>Extending class should override both this method and [.getDividerDrawableAfterImpl]
     * @return The drawable to draw **above/toLeftOf** the item with index 'itemIndex'
     */
    protected open fun getDividerBefore(child: View, realIndex: Int, parent: RecyclerView): ListDivider? {
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
    protected open fun getDividerAfter(child: View, realIndex: Int, parent: RecyclerView): ListDivider? {
        return mDivider
    }

    protected open fun isTopLeftItem(child: View, itemIndex: Int, parent: RecyclerView): Boolean {
        return when (mOrientation) {
            ListDividerOrientation.VERTICAL, ListDividerOrientation.HORIZONTAL
            -> itemIndex == 0

            ListDividerOrientation.GRID_VERTICAL, ListDividerOrientation.GRID_HORIZONTAL
            -> itemIndex < getTopRowItemCount(parent)//columnCount

            else -> false
        }
    }

    protected open fun isBottomRightItem(child: View, itemIndex: Int, parent: RecyclerView): Boolean {
        val totalItemCount = parent.adapter?.itemCount ?: 0

        return when (mOrientation) {
            ListDividerOrientation.VERTICAL, ListDividerOrientation.HORIZONTAL
            -> itemIndex == totalItemCount - 1

            ListDividerOrientation.GRID_VERTICAL, ListDividerOrientation.GRID_HORIZONTAL
            -> itemIndex > totalItemCount - 1 - getBottomRowItemCount(parent)//columnCount

            else -> false
        }
    }










    private fun getDividerBeforeIntl(child: View, realIndex: Int, parent: RecyclerView): ListDivider? {
        return if (isTopLeftItem(child, realIndex, parent))
            getTopDivider(child, parent)
        else getDividerBefore(child, realIndex, parent)
    }

    private fun getDividerAfterIntl(child: View, realIndex: Int, parent: RecyclerView): ListDivider? {
        return if (isBottomRightItem(child, realIndex, parent)) {
            getBottomDivider(child, parent)
        } else getDividerAfter(child, realIndex, parent)
    }



    /**
     * Convenience method. Calls [.getDividerDrawableBefore]
     * with `realIndex = parent.getChildAdapterPosition(child)`
     */
    private fun getDividerDrawableBefore(child: View, parent: RecyclerView): ListDivider? {
        return getDividerBeforeIntl(child, parent.getChildAdapterPosition(child), parent)
    }

    /**
     * Convenience method. Calls [.getDividerDrawableAfter]
     * with `realIndex = parent.getChildAdapterPosition(child)`
     */
    private fun getDividerDrawableAfter(child: View, parent: RecyclerView): ListDivider? {
        return getDividerAfterIntl(child, parent.getChildAdapterPosition(child), parent)
    }

    private fun drawChildDividers(child: View, realIndex: Int, c: Canvas, parent: RecyclerView, orientation: @ListDividerOrientation Int) {
        val itemCount = parent.adapter?.itemCount ?: 0
        val parentBounds = getParentBounds(parent)

        //draw before child
        val dividerBefore = getDividerBeforeIntl(child, realIndex, parent)
        val canvasDrawable = GradientDrawable()

        if (dividerBefore != null) {
            for (bounds in calcChildDividerBoundsBefore(child, realIndex, itemCount, dividerBefore, parentBounds, orientation)) {
                canvasDrawable.bounds = bounds
                canvasDrawable.draw(c)
                //TODO draw (color) not working
            }
        }

        //draw after child
        val dividerAfter = getDividerAfterIntl(child, realIndex, parent)
        if (dividerAfter != null) {
            for (bounds in calcChildDividerBoundsAfter(child, realIndex, itemCount, dividerAfter, parentBounds, orientation)) {
                canvasDrawable.bounds = bounds
                canvasDrawable.draw(c)
                //TODO draw (color) not working
            }
        }
    }

    private fun drawDividers(c: Canvas, parent: RecyclerView, orientation: @ListDividerOrientation Int) {
        val childCount = parent.childCount

        val firstVisible = (if (parent.childCount > 0) parent.getChildAt(0) else null) ?: return

        val firstVisibleRealPosition = parent.getChildAdapterPosition(firstVisible)

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val realIndex = i + firstVisibleRealPosition

            drawChildDividers(child, realIndex, c, parent, orientation)
        }
    }

    private fun getTopRowItemCount(parent: RecyclerView): Int {
        if (cachedGridTopRowItemCount > -1) {
            return cachedGridTopRowItemCount
        }

        val totalItemCount = parent.adapter?.itemCount ?: 0
        var realColumnCount = 1//default 1

        if (totalItemCount > 1) {
            when (mOrientation) {
                ListDividerOrientation.GRID_VERTICAL, ListDividerOrientation.GRID_HORIZONTAL
                -> {
                    val layoutManager = parent.layoutManager

                    if (layoutManager is GridLayoutManager) {
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
                            Logger.w(NewDividerItemDecoration::class
                                , "getTopRowItemCount(): unsupported Grid LayoutManager of class: ${layoutManager?.javaClass?.simpleName}"
                            )
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

    private fun getBottomRowItemCount(parent: RecyclerView): Int {
        if (cachedGridBottomRowItemCount > -1) {
            return cachedGridBottomRowItemCount
        }

        val totalItemCount = parent.adapter?.itemCount ?: 0
        var realColumnCount = 1//default 1

        if (totalItemCount > 1) {
            when (mOrientation) {
                ListDividerOrientation.GRID_VERTICAL, ListDividerOrientation.GRID_HORIZONTAL
                -> {
                    val layoutManager = parent.layoutManager

                    if (layoutManager is GridLayoutManager) {
                        //just get the index+1 of the last item, inside it's span group(of size spanCount())
                        realColumnCount = layoutManager.spanSizeLookup.getSpanIndex(totalItemCount - 1, layoutManager.spanCount) + 1
                    } else {
                        if (BuildConfig.DEBUG) {
                            Logger.w(NewDividerItemDecoration::class, "getBottomRowItemCount(): unsupported Grid LayoutManager of class: " + layoutManager?.javaClass?.simpleName)
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

    private fun addGridItemCountCacheDataObserver(parent: RecyclerView) {
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

    private fun removeGridItemCountCacheDataObserver(parent: RecyclerView) {
        gridItemCountCacheDataObserver?.let {
            parent.adapter?.unregisterAdapterDataObserver(it)
        }
    }
}