package com.guymichael.reactdroid.extensions.components.list.dividers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

internal fun calcVerticalDividerBoundsBelow(child: View, divider: ListDivider, parentBounds: Rect): Rect {
    //draw below child
    val params = child.layoutParams as RecyclerView.LayoutParams
    val top = child.bottom + params.bottomMargin
    val bottom = top + divider.heightPx

    return Rect(parentBounds.left, top, parentBounds.right, bottom)
}

internal fun calcHorizontalDividerBoundsToRightOf(child: View, divider: ListDivider, parentBounds: Rect): Rect {
    //draw to right of child
    val params = child.layoutParams as RecyclerView.LayoutParams
    val left = child.right + params.rightMargin
    val right = left + divider.widthPx

    return Rect(left, parentBounds.top, right, parentBounds.bottom)
}

internal fun calcVerticalDividerBoundsAbove(child: View, divider: ListDivider, parentBounds: Rect): Rect {
    //draw below child
    val params = child.layoutParams as RecyclerView.LayoutParams
    val bottom = child.top - params.topMargin//maybe topMargin should be ignored as it's possibly counted with getTop
    val top = bottom - divider.heightPx

    return Rect(parentBounds.left, top, parentBounds.right, bottom)
}

internal fun calcHorizontalDividerBoundsToLeftOf(child: View, divider: ListDivider, parentBounds: Rect): Rect {
    //draw to left of child
    val params = child.layoutParams as RecyclerView.LayoutParams
    val right = child.left - params.leftMargin//maybe leftMargin should be ignored as it's possibly counted with getLeft
    val left = right - divider.widthPx

    return Rect(left, parentBounds.top, right, parentBounds.bottom)
}

internal fun calcChildDividerBoundsBefore(child: View, realIndex: Int, itemCount: Int
        , divider: ListDivider, parentBounds: Rect, orientation: @ListDividerOrientation Int
    ): List<Rect> {

    val bounds = ArrayList<Rect>()

    when (orientation) {
        ListDividerOrientation.VERTICAL
        -> bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))

        ListDividerOrientation.HORIZONTAL
        -> bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))

        ListDividerOrientation.GRID_VERTICAL
        -> {
            bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))
            bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))
        }

        ListDividerOrientation.GRID_HORIZONTAL
        -> {
            bounds.add(calcVerticalDividerBoundsAbove(child, divider, parentBounds))
            bounds.add(calcHorizontalDividerBoundsToLeftOf(child, divider, parentBounds))
        }

        else -> {}
    }

    return bounds
}

internal fun calcChildDividerBoundsAfter(child: View, realIndex: Int, itemCount: Int
        , divider: ListDivider, parentBounds: Rect, orientation: @ListDividerOrientation Int
    ): List<Rect> {

    val bounds = ArrayList<Rect>()
    when (orientation) {
        ListDividerOrientation.VERTICAL
        -> bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))

        ListDividerOrientation.HORIZONTAL
        -> bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))

        ListDividerOrientation.GRID_VERTICAL
        -> {
            bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))
            bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))
        }

        ListDividerOrientation.GRID_HORIZONTAL
        -> {
            bounds.add(calcVerticalDividerBoundsBelow(child, divider, parentBounds))
            bounds.add(calcHorizontalDividerBoundsToRightOf(child, divider, parentBounds))
        }

        else -> {}
    }

    return bounds
}

internal fun getParentBounds(parent: RecyclerView): Rect {
    return Rect(parent.paddingLeft, parent.paddingTop, parent.width - parent.paddingRight, parent.height - parent.paddingBottom)
}