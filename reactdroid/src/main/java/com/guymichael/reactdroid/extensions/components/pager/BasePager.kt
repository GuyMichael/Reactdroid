package com.guymichael.reactdroid.extensions.components.pager

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent

abstract class BasePager<P : OwnProps, S : OwnState, A : RecyclerView.Adapter<*>>(
        v: ViewPager2
    ) : AComponent<P, S, ViewPager2>(v) {

    /** Called from inside (first) render() to create the adapter. If your adapter requires items
     * as props, call adapter.notifyDataSetChanged immediately to apply the render. */
    protected abstract fun render_createAdapter(): A
    /** Called from inside (non-first) render(), to call your adapter's notifyOnDataSetChanged */
    protected abstract fun render_notifyAdapterOnDataSetChanged(adapter: A)

    @Suppress("UNCHECKED_CAST")
    final override fun render() {
        mView.adapter?.also {
            //re-render, possibly due to itemCount changed //NOCOMMIT QA
            render_notifyAdapterOnDataSetChanged(it as A)

        } ?: run {
            //first render, just apply the adapter
            mView.adapter = render_createAdapter()
        }
    }
}