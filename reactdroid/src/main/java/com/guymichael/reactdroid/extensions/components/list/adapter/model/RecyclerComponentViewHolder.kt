package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.view.View
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.core.model.AComponent

class RecyclerComponentViewHolder(itemView: View)
    : BaseRecyclerComponentViewHolder<ListItemProps<*>>(itemView) {

    private lateinit var mComponent: AComponent<*, *, *>

    override fun bind(item: ListItemProps<*>) {
        val component = getOrCreateComponent(item)

        try {
            component.onRenderOrThrow(item.props)
        } catch (e: IllegalArgumentException) {
            if (BuildConfig.DEBUG) {
                throw e//rethrow
            } else {
                Logger.e(RecyclerComponentViewHolder::class
                    , "bind failed: give props (${item.javaClass.simpleName}) " +
                        "differs from ${component.javaClass.simpleName}'s props"
                )
            }
        }
    }

    private fun getOrCreateComponent(item: ListItemProps<*>): AComponent<*, *, *> {
        //THINK lateinitvar.getOrNull(). Also, try may be better ?
        if( !this::mComponent.isInitialized) {
            mComponent = item.initial_componentCreator(itemView)
        }

        return mComponent
    }
}