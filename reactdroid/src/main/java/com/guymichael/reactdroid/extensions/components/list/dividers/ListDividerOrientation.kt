package com.guymichael.reactdroid.extensions.components.list.dividers

import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView

@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    ListDividerOrientation.HORIZONTAL,
    ListDividerOrientation.VERTICAL,
    ListDividerOrientation.GRID_HORIZONTAL,
    ListDividerOrientation.GRID_VERTICAL
)
annotation class ListDividerOrientation {
    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL //0
        const val VERTICAL = RecyclerView.VERTICAL //1

        const val GRID_HORIZONTAL = 2
        const val GRID_VERTICAL = 3

        //note: these have the exact same reference (to RecyclerView.V or H)
//        const val GRID_VERTICAL_LIST = GridLayoutManager.VERTICAL
//        const val GRID_HORIZONTAL_LIST = GridLayoutManager.HORIZONTAL
    }
}