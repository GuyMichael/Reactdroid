package com.guymichael.reactdroid.extensions.list.adapter

/**
 * Routes all callbacks to [.onChanged]
 */
internal open class SimpleAdapterDataObserver : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        onChanged()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onChanged()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        onChanged()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        onChanged()
    }
}
