package com.guymichael.reactdroid.extensions.components.list

import androidx.recyclerview.widget.RecyclerView
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.list.model.BaseListProps

abstract class BaseListComponent<P : BaseListProps, S : OwnState, V : RecyclerView>(v: V)
    : AComponent<P, S, V>(v) {

    abstract val adapter: RecyclerComponentAdapter

    override fun render() {
        //THINK get actual diff indexes and use efficient methods (e.g. notifyItemRangeChanged() )
        // OR, consider extending ListAdapter to make use of DiffUtil, which calculates list diffs
        // efficiently and on a background thread
        adapter.notifyDataSetChanged(props.items)
    }
}