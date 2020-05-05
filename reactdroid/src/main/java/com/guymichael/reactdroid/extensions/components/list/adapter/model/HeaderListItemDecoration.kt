package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.dividers.ListDivider
import com.guymichael.reactdroid.extensions.components.list.dividers.ListDividerOrientation
import com.guymichael.reactdroid.extensions.components.list.dividers.NewDividerItemDecoration

/**
 * Supports vertical lists only.
 * This decoration differentiates dividers between a list-header view type, and a normal item
 */
class HeaderListItemDecoration(
        divider: ListDivider
        , headerToPrevItemDivider: ListDivider
        , itemToPrevHeaderDivider: ListDivider
        , private val mHeaderClass: Class<out View?>
    ) : NewDividerItemDecoration(ListDividerOrientation.VERTICAL, divider
        , headerToPrevItemDivider, itemToPrevHeaderDivider
    ) {


    override fun isTopLeftItem(child: View, itemIndex: Int, parent: RecyclerView): Boolean {
        val prevLayoutPos = parent.getChildLayoutPosition(child) - 1
        val prevChild = if (prevLayoutPos >= 0) parent.getChildAt(prevLayoutPos) else null

        if (prevChild != null) {
            if (mHeaderClass.isInstance(child) && !mHeaderClass.isInstance(prevChild)) {
                //next item IS header, and prev ISN'T header. use special header-after-item divider
                return true
            }
        }

        //we can't know which divider to use right now :/ or maybe it's the last item
        return super.isTopLeftItem(child, itemIndex, parent)
    }

    override fun isBottomRightItem(child: View, itemIndex: Int, parent: RecyclerView): Boolean {
        val nextLayoutPos = parent.getChildLayoutPosition(child) + 1
        val nextChild = if (nextLayoutPos < parent.childCount)
            parent.getChildAt(nextLayoutPos) else null

        if (nextChild != null) {
            if (!mHeaderClass.isInstance(child) and mHeaderClass.isInstance(nextChild)) {
                //next item ISN'T header. prev IS header, use special item-after-header divider
                return true
            }
        }

        //we can't know which divider to use right now :/ or maybe it's the last item
        return super.isBottomRightItem(child, itemIndex, parent)
    }

    override fun getTopDivider(child: View, parent: RecyclerView): ListDivider? {
        return null
    }

    override fun getBottomDivider(child: View, parent: RecyclerView): ListDivider? {
        return null
    }
}