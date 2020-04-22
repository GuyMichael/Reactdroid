package com.guymichael.reactdroid.extensions.components.list

import androidx.recyclerview.widget.RecyclerView
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import com.guymichael.reactdroid.model.AComponent

abstract class BaseListComponent<P : BaseListProps, S : OwnState, V : RecyclerView>(v: V)
    : AComponent<P, S, V>(v) {

    abstract val adapter: RecyclerComponentAdapter

    override fun render() {
        //THINK get actual diff indexes and use efficient methods (e.g. notifyItemRangeChanged() )
        adapter.notifyDataSetChanged(props.items)
    }
}