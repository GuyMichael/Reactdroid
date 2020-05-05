package com.guymichael.reactdroid.extensions.components.list

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.guymichael.reactdroid.extensions.components.list.dividers.ListDividerOrientation

object ComponentListUtils {
    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * <br></br>For [FlexboxLayoutManager], returns [FlexboxLayoutManager.getFlexDirection]
     * @see OrientationHelper.VERTICAL
     *
     * @return [RecyclerView.Orientation] or [FlexDirection]
     */
    fun getOrientation(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            //linear and grid (inherits linear)
            is LinearLayoutManager -> layoutManager.orientation
            is FlexboxLayoutManager -> layoutManager.flexDirection
            else -> RecyclerView.NO_POSITION
        }
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * @return adapter position of the first fully visible item, or [RecyclerView.NO_POSITION]
     */
    fun getFirstVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            //linear and grid (inherits linear)
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is FlexboxLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }
    }

    /****NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * @return adapter position of the first fully visible item, or [RecyclerView.NO_POSITION]
     */
    fun getLastVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            //linear and grid (inherits linear)
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is FlexboxLayoutManager -> layoutManager.findLastVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * @return adapter position of the first fully visible item, or [RecyclerView.NO_POSITION]
     */
    fun getFirstFullyVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            //linear and grid (inherits linear)
            is LinearLayoutManager -> layoutManager.findFirstCompletelyVisibleItemPosition()
            is FlexboxLayoutManager -> layoutManager.findFirstCompletelyVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * @return adapter position of the last fully visible item, or [RecyclerView.NO_POSITION]
     */
    fun getLastFullyVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            //linear and grid (inherits linear)
            is LinearLayoutManager -> layoutManager.findLastCompletelyVisibleItemPosition()
            is FlexboxLayoutManager -> layoutManager.findLastCompletelyVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }
    }

    fun getVisibleItemCount(layoutManager: RecyclerView.LayoutManager): Int {
        return getLastVisiblePosition(layoutManager) - getFirstVisiblePosition(layoutManager) + 1
    }

    /****NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     */
    fun getDividerOrientation(layoutManager: RecyclerView.LayoutManager): @ListDividerOrientation Int {
        return when (layoutManager) {
            is GridLayoutManager -> {
                when (layoutManager.orientation) {
                    RecyclerView.VERTICAL -> ListDividerOrientation.GRID_VERTICAL
                    RecyclerView.HORIZONTAL -> ListDividerOrientation.GRID_HORIZONTAL
                    else -> ListDividerOrientation.GRID_VERTICAL
                }
            }

            is LinearLayoutManager -> layoutManager.orientation //same ref

            is FlexboxLayoutManager -> {
                when (layoutManager.flexDirection) {
                    FlexDirection.COLUMN, FlexDirection.COLUMN_REVERSE
                    -> ListDividerOrientation.VERTICAL

                    FlexDirection.ROW, FlexDirection.ROW_REVERSE
                    -> ListDividerOrientation.HORIZONTAL

                    else -> ListDividerOrientation.VERTICAL
                }
            }
            else -> RecyclerView.NO_POSITION
        }
    }

    //recycler holds only visible items
    fun getFirstVisibleView(recyclerView: RecyclerView): View? {
        return if (recyclerView.childCount > 0) {
            recyclerView.getChildAt(0)
        } else {
            null
        }
    }

    //recycler holds only visible items
    fun getLastVisibleView(recyclerView: RecyclerView): View? {
        return if (recyclerView.childCount > 0) {
            recyclerView.getChildAt(recyclerView.childCount - 1)
        } else {
            null
        }
    }

    fun getFirstFullyVisibleView(layoutManager: RecyclerView.LayoutManager, recyclerView: RecyclerView): View? {
        val position = getFirstFullyVisiblePosition(layoutManager)

        return if (recyclerView.childCount > 0 && position >= 0) {
            recyclerView.getChildAt(position)
        } else {
            null
        }
    }

    fun getLastFullyVisibleView(layoutManager: RecyclerView.LayoutManager, recyclerView: RecyclerView): View? {
        val position = getLastFullyVisiblePosition(layoutManager)

        return if (recyclerView.childCount > 0 && position >= 0) {
            recyclerView.getChildAt(position)
        } else {
            null
        }
    }
}