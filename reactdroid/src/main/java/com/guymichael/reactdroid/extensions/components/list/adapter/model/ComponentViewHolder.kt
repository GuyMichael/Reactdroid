package com.guymichael.reactdroid.extensions.components.list.adapter.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.list.model.AdapterItemProps

class ComponentViewHolder<T : AdapterItemProps>(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    private lateinit var mComponent: AComponent<*, *, *>

    fun bind(item: T) {
        val component = getOrCreateComponent(item)

        try {
            component.onRenderOrThrow(item.props)
        } catch (e: IllegalArgumentException) {
            if (BuildConfig.DEBUG) {
                throw e//rethrow
            } else {
                Logger.e(ComponentViewHolder::class
                    , "bind failed: give props (${item.javaClass.simpleName}) " +
                        "differs from ${component.javaClass.simpleName}'s props"
                )
            }
        }
    }

    private fun getOrCreateComponent(item: T): AComponent<*, *, *> {
        //THINK lateinitvar.getOrNull(). Also, try may be better ?
        if( !this::mComponent.isInitialized) {
            mComponent = item.initial_componentCreator(itemView)
        }

        return mComponent
    }
}