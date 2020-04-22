package com.guymichael.reactdroid.extensions.components.list.adapter

import android.view.View
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.model.AComponent

class RecyclerComponentViewHolder(itemView: View)
    : BaseRecyclerComponentViewHolder(itemView) {

    private lateinit var mComponent: AComponent<*, *, *>

    override fun bind(props: ListItemProps) {
        val component = getOrCreateComponent(props)

        try {
            component.onRenderOrThrow(props.mapToComponentProps())
        } catch (e: IllegalArgumentException) {
            if (BuildConfig.DEBUG) {
                throw e//rethrow
            } else {
                Logger.e(RecyclerComponentViewHolder::class, "bind failed: give props (${props.javaClass.simpleName}) " +
                        "differs from ${component.javaClass.simpleName}'s props")
            }
        }
    }

    private fun getOrCreateComponent(props: ListItemProps): AComponent<*, *, *> {
        //THINK lateinitvar.getOrNull(). Also, try may be better ?
        if( !this::mComponent.isInitialized) {
            mComponent = props.createComponent(itemView)
        }

        return mComponent
    }
}