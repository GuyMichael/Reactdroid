package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.model.AdapterItemProps
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps

abstract class BaseRecyclerComponentViewHolder<T : AdapterItemProps>(itemView: View)
    : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)
}