package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps

abstract class BaseRecyclerComponentViewHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(props: ListItemProps)
}