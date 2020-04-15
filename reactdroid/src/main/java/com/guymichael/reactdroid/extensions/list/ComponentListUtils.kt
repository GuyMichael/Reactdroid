package com.guymichael.reactdroid.extensions.list

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager

class ComponentListUtils{companion object {
    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     * <br></br>For [FlexboxLayoutManager], returns [FlexboxLayoutManager.getFlexDirection]
     * @see OrientationHelper.VERTICAL
     *
     */
    fun getOrientation(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.orientation
                ?: ((layoutManager as? LinearLayoutManager)?.orientation
                ?: (layoutManager as FlexboxLayoutManager).flexDirection)
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     */
    //GridLayout
    fun getFirstVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.findFirstVisibleItemPosition()
                ?: ((layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                ?: (layoutManager as FlexboxLayoutManager).findFirstVisibleItemPosition())
    }

    /****NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     */
    fun getLastVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.findLastVisibleItemPosition()
                ?: ((layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                ?: (layoutManager as FlexboxLayoutManager).findLastVisibleItemPosition())
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     */
    fun getFirstFullyVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                ?: ((layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                ?: (layoutManager as FlexboxLayoutManager).findFirstCompletelyVisibleItemPosition())
    }

    /**
     * **NOTE:** Currently only works with
     * [LinearLayoutManager], [GridLayoutManager] & [FlexboxLayoutManager] !
     */
    fun getLastFullyVisiblePosition(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.findLastCompletelyVisibleItemPosition()
                ?: ((layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                ?: (layoutManager as FlexboxLayoutManager).findLastCompletelyVisibleItemPosition())
    }

    fun getVisibleItemCount(layoutManager: RecyclerView.LayoutManager): Int {
        return getLastVisiblePosition(layoutManager) - getFirstVisiblePosition(layoutManager) + 1
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
}}